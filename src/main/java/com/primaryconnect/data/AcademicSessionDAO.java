package com.primaryconnect.data;

import com.primaryconnect.model.AcademicSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides data access operations for academic session lookup records.
 */
public class AcademicSessionDAO {
    private static final String INSERT_SQL = """
            INSERT INTO academic_sessions (label, start_year, end_year)
            VALUES (?, ?, ?)
            """;
    private static final String FIND_BY_LABEL_SQL = """
            SELECT session_id, label, start_year, end_year
            FROM academic_sessions
            WHERE label = ?
            """;

    public AcademicSessionDAO() {
    }

    public int findOrCreateByLabel(String label) {
        AcademicSessionRecord existing = findByLabel(label);
        if (existing != null) {
            return existing.sessionId();
        }

        AcademicSession session = AcademicSession.parse(label);
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, session.toString());
            statement.setInt(2, session.startYear());
            statement.setInt(3, session.endYear());
            statement.executeUpdate();

            return readGeneratedId(statement, "create academic session");
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create academic session.", exception);
        }
    }

    public AcademicSessionRecord findByLabel(String label) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_LABEL_SQL)) {
            statement.setString(1, label);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new AcademicSessionRecord(
                            resultSet.getInt("session_id"),
                            resultSet.getString("label"),
                            resultSet.getInt("start_year"),
                            resultSet.getInt("end_year")
                    );
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find academic session by label " + label + ".", exception);
        }
    }

    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    private int readGeneratedId(PreparedStatement statement, String operation) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        throw new RuntimeException("Failed to " + operation + ": no generated ID returned.");
    }

    public record AcademicSessionRecord(int sessionId, String label, int startYear, int endYear) {
    }
}
