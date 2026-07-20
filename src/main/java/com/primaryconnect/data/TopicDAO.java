package com.primaryconnect.data;

import com.primaryconnect.model.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for topic records.
 */
public class TopicDAO {
    private static final String INSERT_SQL = """
            INSERT INTO topics (subject_id, class_level, title, media_path)
            VALUES (?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT topic_id, subject_id, class_level, title, media_path
            FROM topics
            WHERE topic_id = ?
            """;
    private static final String FIND_BY_SUBJECT_CLASS_LEVEL_TITLE_SQL = """
            SELECT topic_id, subject_id, class_level, title, media_path
            FROM topics
            WHERE subject_id = ? AND class_level = ? AND title = ?
            """;
    private static final String FIND_BY_SUBJECT_AND_CLASS_LEVEL_SQL = """
            SELECT topic_id, subject_id, class_level, title, media_path
            FROM topics
            WHERE subject_id = ? AND class_level = ?
            ORDER BY title, topic_id
            """;

    public TopicDAO() {
    }

    public int create(Topic topic) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, topic.getSubjectId());
            statement.setString(2, topic.getClassLevel());
            statement.setString(3, topic.getTitle());
            statement.setString(4, topic.getMediaPath());
            statement.executeUpdate();

            int topicId = readGeneratedId(statement, "create topic");
            topic.setTopicId(topicId);
            return topicId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create topic.", exception);
        }
    }

    public Topic findById(int topicId) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, topicId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTopic(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find topic by ID " + topicId + ".", exception);
        }
    }

    public Topic findBySubjectClassLevelTitle(int subjectId, String classLevel, String title) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_SUBJECT_CLASS_LEVEL_TITLE_SQL)) {
            statement.setInt(1, subjectId);
            statement.setString(2, classLevel);
            statement.setString(3, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTopic(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find topic for subject ID " + subjectId + ", class level " + classLevel
                            + ", title " + title + ".",
                    exception
            );
        }
    }

    public List<Topic> findBySubjectAndClassLevel(int subjectId, String classLevel) {
        List<Topic> topics = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_SUBJECT_AND_CLASS_LEVEL_SQL)) {
            statement.setInt(1, subjectId);
            statement.setString(2, classLevel);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    topics.add(mapTopic(resultSet));
                }
            }
            return topics;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find topics for subject ID " + subjectId + " and class level " + classLevel + ".",
                    exception
            );
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

    private Topic mapTopic(ResultSet resultSet) throws SQLException {
        return new Topic(
                resultSet.getInt("topic_id"),
                resultSet.getInt("subject_id"),
                resultSet.getString("class_level"),
                resultSet.getString("title"),
                resultSet.getString("media_path")
        );
    }
}
