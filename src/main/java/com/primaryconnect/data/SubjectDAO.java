package com.primaryconnect.data;

import com.primaryconnect.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for subject records.
 */
public class SubjectDAO {
    private static final String INSERT_SQL = """
            INSERT INTO subjects (name)
            VALUES (?)
            """;
    private static final String FIND_BY_NAME_SQL = """
            SELECT subject_id, name
            FROM subjects
            WHERE name = ?
            """;
    private static final String FIND_ALL_SQL = """
            SELECT subject_id, name
            FROM subjects
            ORDER BY name, subject_id
            """;

    public SubjectDAO() {
    }

    public int create(Subject subject) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subject.getName());
            statement.executeUpdate();

            int subjectId = readGeneratedId(statement, "create subject");
            subject.setSubjectId(subjectId);
            return subjectId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create subject.", exception);
        }
    }

    public Subject findByName(String name) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_NAME_SQL)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapSubject(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find subject by name " + name + ".", exception);
        }
    }

    public int findOrCreateByName(String name) {
        Subject existingSubject = findByName(name);
        if (existingSubject != null) {
            return existingSubject.getSubjectId();
        }

        return create(new Subject(0, name));
    }

    public List<Subject> findAll() {
        List<Subject> subjects = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                subjects.add(mapSubject(resultSet));
            }
            return subjects;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find all subjects.", exception);
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

    private Subject mapSubject(ResultSet resultSet) throws SQLException {
        return new Subject(
                resultSet.getInt("subject_id"),
                resultSet.getString("name")
        );
    }
}
