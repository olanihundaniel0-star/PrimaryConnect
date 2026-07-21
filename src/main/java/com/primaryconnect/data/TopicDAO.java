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
    private static final String TOPIC_COLUMNS = """
            topic_id, subject_id, class_level, title, term, week, learning_objectives, contents,
            teacher_activities, learner_activities, teaching_materials, assessment, media_path
            """;
    private static final String INSERT_SQL = """
            INSERT INTO topics (
                subject_id, class_level, title, term, week, learning_objectives, contents,
                teacher_activities, learner_activities, teaching_materials, assessment, media_path
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT %s
            FROM topics
            WHERE topic_id = ?
            """.formatted(TOPIC_COLUMNS);
    private static final String FIND_BY_SUBJECT_CLASS_LEVEL_TERM_TITLE_SQL = """
            SELECT %s
            FROM topics
            WHERE subject_id = ? AND class_level = ? AND term = ? AND title = ?
            """.formatted(TOPIC_COLUMNS);
    private static final String FIND_BY_TITLE_SQL = """
            SELECT %s
            FROM topics
            WHERE title = ?
            ORDER BY topic_id
            """.formatted(TOPIC_COLUMNS);
    private static final String FIND_BY_SUBJECT_CLASS_LEVEL_TERM_SQL = """
            SELECT %s
            FROM topics
            WHERE subject_id = ? AND class_level = ? AND term = ?
            ORDER BY week, title, topic_id
            """.formatted(TOPIC_COLUMNS);

    public TopicDAO() {
    }

    public int create(Topic topic) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, topic.getSubjectId());
            statement.setString(2, topic.getClassLevel());
            statement.setString(3, topic.getTitle());
            statement.setString(4, topic.getTerm());
            setNullableInteger(statement, 5, topic.getWeek());
            statement.setString(6, topic.getLearningObjectives());
            statement.setString(7, topic.getContents());
            statement.setString(8, topic.getTeacherActivities());
            statement.setString(9, topic.getLearnerActivities());
            statement.setString(10, topic.getTeachingMaterials());
            statement.setString(11, topic.getAssessment());
            statement.setString(12, topic.getMediaPath());
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

    public Topic findBySubjectClassLevelTermTitle(int subjectId, String classLevel, String term, String title) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_SUBJECT_CLASS_LEVEL_TERM_TITLE_SQL)) {
            statement.setInt(1, subjectId);
            statement.setString(2, classLevel);
            statement.setString(3, term);
            statement.setString(4, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTopic(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find topic for subject ID " + subjectId + ", class level " + classLevel
                            + ", term " + term + ", title " + title + ".",
                    exception
            );
        }
    }

    public Topic findByTitle(String title) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_TITLE_SQL)) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTopic(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find topic by title " + title + ".", exception);
        }
    }

    public List<Topic> findBySubjectClassLevelTerm(int subjectId, String classLevel, String term) {
        List<Topic> topics = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_SUBJECT_CLASS_LEVEL_TERM_SQL)) {
            statement.setInt(1, subjectId);
            statement.setString(2, classLevel);
            statement.setString(3, term);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    topics.add(mapTopic(resultSet));
                }
            }
            return topics;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find topics for subject ID " + subjectId + ", class level " + classLevel
                            + ", and term " + term + ".",
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

    private void setNullableInteger(PreparedStatement statement, int parameterIndex, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, java.sql.Types.INTEGER);
            return;
        }
        statement.setInt(parameterIndex, value);
    }

    private Topic mapTopic(ResultSet resultSet) throws SQLException {
        int week = resultSet.getInt("week");
        boolean weekWasNull = resultSet.wasNull();
        return new Topic(
                resultSet.getInt("topic_id"),
                resultSet.getInt("subject_id"),
                resultSet.getString("class_level"),
                resultSet.getString("title"),
                resultSet.getString("term"),
                weekWasNull ? null : week,
                resultSet.getString("learning_objectives"),
                resultSet.getString("contents"),
                resultSet.getString("teacher_activities"),
                resultSet.getString("learner_activities"),
                resultSet.getString("teaching_materials"),
                resultSet.getString("assessment"),
                resultSet.getString("media_path")
        );
    }
}
