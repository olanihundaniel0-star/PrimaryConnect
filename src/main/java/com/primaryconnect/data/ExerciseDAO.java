package com.primaryconnect.data;

import com.primaryconnect.model.Exercise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for exercise and curriculum content.
 */
public class ExerciseDAO {
    private static final String INSERT_SQL = """
            INSERT INTO exercises (topic_id, type, question_text, options, correct_answer)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT exercise_id, topic_id, type, question_text, options, correct_answer
            FROM exercises
            WHERE exercise_id = ?
            """;
    private static final String FIND_BY_TOPIC_SQL = """
            SELECT exercise_id, topic_id, type, question_text, options, correct_answer
            FROM exercises
            WHERE topic_id = ?
            ORDER BY exercise_id
            """;
    private static final String UPDATE_SQL = """
            UPDATE exercises
            SET topic_id = ?, type = ?, question_text = ?, options = ?, correct_answer = ?
            WHERE exercise_id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM exercises
            WHERE exercise_id = ?
            """;

    public ExerciseDAO() {
    }

    public int create(Exercise exercise) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, exercise.getTopicId());
            statement.setString(2, exercise.getType());
            statement.setString(3, exercise.getQuestionText());
            statement.setString(4, exercise.getOptions());
            statement.setString(5, exercise.getCorrectAnswer());
            statement.executeUpdate();

            int exerciseId = readGeneratedId(statement, "create exercise");
            exercise.setExerciseId(exerciseId);
            return exerciseId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create exercise.", exception);
        }
    }

    public Exercise findById(int exerciseId) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, exerciseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapExercise(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find exercise by ID " + exerciseId + ".", exception);
        }
    }

    public List<Exercise> findByTopic(int topicId) {
        List<Exercise> exercises = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_TOPIC_SQL)) {
            statement.setInt(1, topicId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    exercises.add(mapExercise(resultSet));
                }
            }
            return exercises;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find exercises for topic ID " + topicId + ".", exception);
        }
    }

    public void update(Exercise exercise) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, exercise.getTopicId());
            statement.setString(2, exercise.getType());
            statement.setString(3, exercise.getQuestionText());
            statement.setString(4, exercise.getOptions());
            statement.setString(5, exercise.getCorrectAnswer());
            statement.setInt(6, exercise.getExerciseId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update exercise with ID " + exercise.getExerciseId() + ".", exception);
        }
    }

    public void delete(int exerciseId) {
        try (PreparedStatement statement = getConnection().prepareStatement(DELETE_SQL)) {
            statement.setInt(1, exerciseId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete exercise with ID " + exerciseId + ".", exception);
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

    private Exercise mapExercise(ResultSet resultSet) throws SQLException {
        return new Exercise(
                resultSet.getInt("exercise_id"),
                resultSet.getInt("topic_id"),
                resultSet.getString("type"),
                resultSet.getString("question_text"),
                resultSet.getString("options"),
                resultSet.getString("correct_answer")
        );
    }
}
