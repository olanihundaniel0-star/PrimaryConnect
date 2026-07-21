package com.primaryconnect.data;

import com.primaryconnect.model.Score;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for score and grading records.
 */
public class ScoreDAO {
    private static final String INSERT_SQL = """
            INSERT INTO scores (pupil_id, subject_id, session, term, test_score, exam_score, final_score, grade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_BY_PUPIL_SUBJECT_TERM_SQL = """
            SELECT score_id, pupil_id, subject_id, session, term, test_score, exam_score, final_score, grade
            FROM scores
            WHERE pupil_id = ? AND subject_id = ? AND session = ? AND term = ?
            """;
    private static final String FIND_ALL_BY_PUPIL_SQL = """
            SELECT score_id, pupil_id, subject_id, session, term, test_score, exam_score, final_score, grade
            FROM scores
            WHERE pupil_id = ?
            ORDER BY session, term, subject_id
            """;
    private static final String UPDATE_SQL = """
            UPDATE scores
            SET pupil_id = ?, subject_id = ?, session = ?, term = ?, test_score = ?, exam_score = ?, final_score = ?, grade = ?
            WHERE score_id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM scores
            WHERE score_id = ?
            """;

    public ScoreDAO() {
    }

    public int create(Score score) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, score.getPupilId());
            statement.setInt(2, score.getSubjectId());
            statement.setString(3, score.getSession());
            statement.setString(4, score.getTerm());
            statement.setDouble(5, score.getTestScore());
            statement.setDouble(6, score.getExamScore());
            statement.setDouble(7, score.getFinalScore());
            statement.setString(8, score.getGrade());
            statement.executeUpdate();

            int scoreId = readGeneratedId(statement, "create score");
            score.setScoreId(scoreId);
            return scoreId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create score.", exception);
        }
    }

    public Score findByPupilSubjectTerm(int pupilId, int subjectId, String session, String term) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_PUPIL_SUBJECT_TERM_SQL)) {
            statement.setInt(1, pupilId);
            statement.setInt(2, subjectId);
            statement.setString(3, session);
            statement.setString(4, term);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapScore(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find score for pupil ID " + pupilId + ", subject ID " + subjectId
                            + ", session " + session + ", term " + term + ".",
                    exception
            );
        }
    }

    public List<Score> findAllByPupil(int pupilId) {
        List<Score> scores = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_ALL_BY_PUPIL_SQL)) {
            statement.setInt(1, pupilId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    scores.add(mapScore(resultSet));
                }
            }
            return scores;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find scores for pupil ID " + pupilId + ".", exception);
        }
    }

    public void update(Score score) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, score.getPupilId());
            statement.setInt(2, score.getSubjectId());
            statement.setString(3, score.getSession());
            statement.setString(4, score.getTerm());
            statement.setDouble(5, score.getTestScore());
            statement.setDouble(6, score.getExamScore());
            statement.setDouble(7, score.getFinalScore());
            statement.setString(8, score.getGrade());
            statement.setInt(9, score.getScoreId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update score with ID " + score.getScoreId() + ".", exception);
        }
    }

    public void delete(int scoreId) {
        try (PreparedStatement statement = getConnection().prepareStatement(DELETE_SQL)) {
            statement.setInt(1, scoreId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete score with ID " + scoreId + ".", exception);
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

    private Score mapScore(ResultSet resultSet) throws SQLException {
        return new Score(
                resultSet.getInt("score_id"),
                resultSet.getInt("pupil_id"),
                resultSet.getInt("subject_id"),
                resultSet.getString("session"),
                resultSet.getString("term"),
                resultSet.getDouble("test_score"),
                resultSet.getDouble("exam_score"),
                resultSet.getDouble("final_score"),
                resultSet.getString("grade")
        );
    }
}
