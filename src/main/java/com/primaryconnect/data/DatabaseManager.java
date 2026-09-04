package com.primaryconnect.data;

import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC/SQLite connection for the application.
 */
public final class DatabaseManager {
    private static final String DATABASE_FILE = "primaryconnect.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_FILE;
    private static final String SCHEMA_RESOURCE = "db/schema.sql";
    private static final String ENABLE_FOREIGN_KEYS_SQL = "PRAGMA foreign_keys = ON;";
    private static final String DISABLE_FOREIGN_KEYS_SQL = "PRAGMA foreign_keys = OFF;";

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private Connection connection;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }
            return connection;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to inspect database connection state.", exception);
        }
    }

    public synchronized void closeConnection() {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to close database connection.", exception);
        } finally {
            connection = null;
        }
    }

    private void openConnection() {
        boolean shouldInitialize = !Files.exists(Path.of(DATABASE_FILE));

        try {
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to connect to database at " + JDBC_URL + ".", exception);
        }

        enableForeignKeys(connection);

        if (shouldInitialize) {
            initializeSchema(connection);
        } else {
            migrateSchemaIfNeeded(connection);
        }
    }

    private void enableForeignKeys(Connection activeConnection) {
        try (Statement statement = activeConnection.createStatement()) {
            statement.execute(ENABLE_FOREIGN_KEYS_SQL);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to enable SQLite foreign key enforcement.", exception);
        }
    }

    private void initializeSchema(Connection activeConnection) {
        String schemaSql = readSchemaSql();

        try (Statement statement = activeConnection.createStatement()) {
            for (String sqlStatement : schemaSql.split(";")) {
                String trimmedStatement = sqlStatement.trim();
                if (!trimmedStatement.isEmpty()) {
                    statement.execute(trimmedStatement);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to run database schema initialization.", exception);
        }
    }

    private void migrateSchemaIfNeeded(Connection activeConnection) {
        if (isNormalizedSchema(activeConnection)) {
            return;
        }

        boolean originalAutoCommit;
        try {
            originalAutoCommit = activeConnection.getAutoCommit();
            activeConnection.setAutoCommit(false);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to prepare schema migration transaction.", exception);
        }

        try (Statement statement = activeConnection.createStatement()) {
            statement.execute(DISABLE_FOREIGN_KEYS_SQL);
            ensureLookupTables(statement);
            seedAcademicTerms(activeConnection);
            seedAcademicSessions(activeConnection);
            rebuildTopicsTable(statement);
            rebuildScoresTable(statement);
            statement.execute(ENABLE_FOREIGN_KEYS_SQL);
            activeConnection.commit();
        } catch (SQLException exception) {
            try {
                activeConnection.rollback();
            } catch (SQLException rollbackException) {
                throw new RuntimeException("Failed to roll back schema migration.", rollbackException);
            }
            throw new RuntimeException("Failed to migrate database schema.", exception);
        } finally {
            try {
                activeConnection.setAutoCommit(originalAutoCommit);
            } catch (SQLException exception) {
                throw new RuntimeException("Failed to restore auto-commit state after migration.", exception);
            }
        }
    }

    private boolean isNormalizedSchema(Connection activeConnection) {
        return tableExists(activeConnection, "academic_terms")
                && tableExists(activeConnection, "academic_sessions")
                && columnExists(activeConnection, "topics", "term_id")
                && columnExists(activeConnection, "scores", "session_id")
                && columnExists(activeConnection, "scores", "term_id");
    }

    private boolean tableExists(Connection activeConnection, String tableName) {
        String sql = """
                SELECT 1
                FROM sqlite_master
                WHERE type = 'table' AND name = ?
                """;

        try (PreparedStatement statement = activeConnection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to inspect whether table " + tableName + " exists.", exception);
        }
    }

    private boolean columnExists(Connection activeConnection, String tableName, String columnName) {
        String sql = "PRAGMA table_info(" + tableName + ");";

        try (Statement statement = activeConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to inspect columns for table " + tableName + ".", exception);
        }
    }

    private void ensureLookupTables(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS academic_terms (
                    term_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT NOT NULL UNIQUE,
                    display_name TEXT NOT NULL UNIQUE,
                    sort_order INTEGER NOT NULL UNIQUE
                )
                """);
        statement.execute("""
                CREATE TABLE IF NOT EXISTS academic_sessions (
                    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    label TEXT NOT NULL UNIQUE,
                    start_year INTEGER NOT NULL,
                    end_year INTEGER NOT NULL
                )
                """);
    }

    private void seedAcademicTerms(Connection activeConnection) {
        String sql = """
                SELECT DISTINCT term
                FROM (
                    SELECT term FROM topics
                    UNION
                    SELECT term FROM scores
                )
                WHERE term IS NOT NULL AND TRIM(term) <> ''
                ORDER BY term
                """;

        String insertSql = """
                INSERT OR IGNORE INTO academic_terms (code, display_name, sort_order)
                VALUES (?, ?, ?)
                """;

        try (Statement statement = activeConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);
             PreparedStatement insertStatement = activeConnection.prepareStatement(insertSql)) {
            while (resultSet.next()) {
                String label = resultSet.getString("term");
                AcademicTerm academicTerm = AcademicTerm.fromLabel(label);
                insertStatement.setString(1, academicTerm.name());
                insertStatement.setString(2, academicTerm.getDisplayName());
                insertStatement.setInt(3, academicTerm.ordinal() + 1);
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to seed academic terms during schema migration.", exception);
        }
    }

    private void seedAcademicSessions(Connection activeConnection) {
        String sql = """
                SELECT DISTINCT session
                FROM scores
                WHERE session IS NOT NULL AND TRIM(session) <> ''
                ORDER BY session
                """;

        String insertSql = """
                INSERT OR IGNORE INTO academic_sessions (label, start_year, end_year)
                VALUES (?, ?, ?)
                """;

        try (Statement statement = activeConnection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql);
             PreparedStatement insertStatement = activeConnection.prepareStatement(insertSql)) {
            while (resultSet.next()) {
                String label = resultSet.getString("session");
                AcademicSession academicSession = AcademicSession.parse(label);
                insertStatement.setString(1, academicSession.toString());
                insertStatement.setInt(2, academicSession.startYear());
                insertStatement.setInt(3, academicSession.endYear());
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to seed academic sessions during schema migration.", exception);
        }
    }

    private void rebuildTopicsTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE topics_new (
                    topic_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject_id INTEGER NOT NULL,
                    class_level TEXT NOT NULL,
                    title TEXT NOT NULL,
                    term_id INTEGER NOT NULL,
                    week INTEGER,
                    learning_objectives TEXT,
                    contents TEXT,
                    teacher_activities TEXT,
                    learner_activities TEXT,
                    teaching_materials TEXT,
                    assessment TEXT,
                    media_path TEXT,
                    UNIQUE (subject_id, class_level, term_id, title),
                    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
                    FOREIGN KEY (term_id) REFERENCES academic_terms(term_id)
                )
                """);
        statement.execute("""
                INSERT INTO topics_new (
                    topic_id, subject_id, class_level, title, term_id, week, learning_objectives, contents,
                    teacher_activities, learner_activities, teaching_materials, assessment, media_path
                )
                SELECT topics.topic_id,
                       topics.subject_id,
                       topics.class_level,
                       topics.title,
                       academic_terms.term_id,
                       topics.week,
                       topics.learning_objectives,
                       topics.contents,
                       topics.teacher_activities,
                       topics.learner_activities,
                       topics.teaching_materials,
                       topics.assessment,
                       topics.media_path
                FROM topics
                INNER JOIN academic_terms ON academic_terms.display_name = topics.term
                """);
        statement.execute("DROP TABLE topics");
        statement.execute("ALTER TABLE topics_new RENAME TO topics");
    }

    private void rebuildScoresTable(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE scores_new (
                    score_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pupil_id INTEGER NOT NULL,
                    subject_id INTEGER NOT NULL,
                    session_id INTEGER NOT NULL,
                    term_id INTEGER NOT NULL,
                    test_score REAL,
                    exam_score REAL,
                    final_score REAL,
                    grade TEXT CHECK (grade IN ('A', 'B', 'C', 'D', 'E', 'F')),
                    UNIQUE (pupil_id, subject_id, session_id, term_id),
                    FOREIGN KEY (pupil_id) REFERENCES pupils(pupil_id),
                    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
                    FOREIGN KEY (session_id) REFERENCES academic_sessions(session_id),
                    FOREIGN KEY (term_id) REFERENCES academic_terms(term_id)
                )
                """);
        statement.execute("""
                INSERT INTO scores_new (
                    score_id, pupil_id, subject_id, session_id, term_id, test_score, exam_score, final_score, grade
                )
                SELECT scores.score_id,
                       scores.pupil_id,
                       scores.subject_id,
                       academic_sessions.session_id,
                       academic_terms.term_id,
                       scores.test_score,
                       scores.exam_score,
                       scores.final_score,
                       scores.grade
                FROM scores
                INNER JOIN academic_sessions ON academic_sessions.label = scores.session
                INNER JOIN academic_terms ON academic_terms.display_name = scores.term
                """);
        statement.execute("DROP TABLE scores");
        statement.execute("ALTER TABLE scores_new RENAME TO scores");
    }

    private String readSchemaSql() {
        ClassLoader classLoader = DatabaseManager.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new RuntimeException("Failed to initialize database: schema resource " + SCHEMA_RESOURCE + " was not found.");
            }

            StringBuilder schemaBuilder = new StringBuilder();
            try (
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    schemaBuilder.append(line).append(System.lineSeparator());
                }
            }
            return schemaBuilder.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read database schema resource " + SCHEMA_RESOURCE + ".", exception);
        }
    }
}
