package com.primaryconnect.data;

import com.primaryconnect.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Provides data access operations for application users.
 */
public class UserDAO {
    private static final String INSERT_SQL = """
            INSERT INTO users (username, password_hash, role, linked_id)
            VALUES (?, ?, ?, ?)
            """;
    private static final String FIND_BY_USERNAME_SQL = """
            SELECT user_id, username, password_hash, role, linked_id
            FROM users
            WHERE username = ?
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT user_id, username, password_hash, role, linked_id
            FROM users
            WHERE user_id = ?
            """;

    public UserDAO() {
    }

    public int create(User user) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRole());
            setNullableInteger(statement, 4, user.getLinkedId());
            statement.executeUpdate();

            int userId = readGeneratedId(statement, "create user");
            user.setUserId(userId);
            return userId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create user.", exception);
        }
    }

    public User findByUsername(String username) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_USERNAME_SQL)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find user by username " + username + ".", exception);
        }
    }

    public User findById(int userId) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapUser(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find user by ID " + userId + ".", exception);
        }
    }

    private Connection getConnection() {
        return DatabaseManager.getInstance().getConnection();
    }

    private void setNullableInteger(PreparedStatement statement, int parameterIndex, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.INTEGER);
        } else {
            statement.setInt(parameterIndex, value);
        }
    }

    private int readGeneratedId(PreparedStatement statement, String operation) throws SQLException {
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        throw new RuntimeException("Failed to " + operation + ": no generated ID returned.");
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int linkedId = resultSet.getInt("linked_id");
        Integer nullableLinkedId = resultSet.wasNull() ? null : linkedId;

        return new User(
                resultSet.getInt("user_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("role"),
                nullableLinkedId
        );
    }
}
