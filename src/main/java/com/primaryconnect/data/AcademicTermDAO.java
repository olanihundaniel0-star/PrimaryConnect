package com.primaryconnect.data;

import com.primaryconnect.model.AcademicTerm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides data access operations for academic term lookup records.
 */
public class AcademicTermDAO {
    private static final String INSERT_SQL = """
            INSERT INTO academic_terms (code, display_name, sort_order)
            VALUES (?, ?, ?)
            """;
    private static final String FIND_BY_DISPLAY_NAME_SQL = """
            SELECT term_id, code, display_name, sort_order
            FROM academic_terms
            WHERE display_name = ?
            """;

    public AcademicTermDAO() {
    }

    public int findOrCreateByDisplayName(String displayName) {
        AcademicTermRecord existing = findByDisplayName(displayName);
        if (existing != null) {
            return existing.termId();
        }

        AcademicTerm academicTerm = AcademicTerm.fromLabel(displayName);
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, academicTerm.name());
            statement.setString(2, academicTerm.getDisplayName());
            statement.setInt(3, academicTerm.ordinal() + 1);
            statement.executeUpdate();

            return readGeneratedId(statement, "create academic term");
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create academic term.", exception);
        }
    }

    public AcademicTermRecord findByDisplayName(String displayName) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_DISPLAY_NAME_SQL)) {
            statement.setString(1, displayName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new AcademicTermRecord(
                            resultSet.getInt("term_id"),
                            resultSet.getString("code"),
                            resultSet.getString("display_name"),
                            resultSet.getInt("sort_order")
                    );
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find academic term by display name " + displayName + ".", exception);
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

    public record AcademicTermRecord(int termId, String code, String displayName, int sortOrder) {
    }
}
