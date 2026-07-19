package com.primaryconnect.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
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
