package com.primaryconnect.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides data access operations for sync log entries.
 */
public class SyncLogDAO {
    private static final String INSERT_SQL = """
            INSERT INTO sync_log (device_id, timestamp, record_count, status)
            VALUES (?, ?, ?, ?)
            """;

    public SyncLogDAO() {
    }

    public int create(String deviceId, String timestamp, int recordCount, String status) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, deviceId);
            statement.setString(2, timestamp);
            statement.setInt(3, recordCount);
            statement.setString(4, status);
            statement.executeUpdate();

            return readGeneratedId(statement, "create sync log entry");
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create sync log entry.", exception);
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
}
