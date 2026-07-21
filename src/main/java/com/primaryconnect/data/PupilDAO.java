package com.primaryconnect.data;

import com.primaryconnect.model.Pupil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for pupil records.
 */
public class PupilDAO {
    private static final String INSERT_SQL = """
            INSERT INTO pupils (name, class_level, guardian_contact)
            VALUES (?, ?, ?)
            """;
    private static final String FIND_BY_ID_SQL = """
            SELECT pupil_id, name, class_level, guardian_contact
            FROM pupils
            WHERE pupil_id = ?
            """;
    private static final String FIND_BY_CLASS_LEVEL_SQL = """
            SELECT pupil_id, name, class_level, guardian_contact
            FROM pupils
            WHERE class_level = ?
            ORDER BY name, pupil_id
            """;
    private static final String UPDATE_SQL = """
            UPDATE pupils
            SET name = ?, class_level = ?, guardian_contact = ?
            WHERE pupil_id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM pupils
            WHERE pupil_id = ?
            """;

    public PupilDAO() {
    }

    public int create(Pupil pupil) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, pupil.getName());
            statement.setString(2, pupil.getClassLevel());
            statement.setString(3, pupil.getGuardianContact());
            statement.executeUpdate();

            int pupilId = readGeneratedId(statement, "create pupil");
            pupil.setPupilId(pupilId);
            return pupilId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create pupil.", exception);
        }
    }

    public Pupil findById(int pupilId) {
        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, pupilId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapPupil(resultSet);
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find pupil by ID " + pupilId + ".", exception);
        }
    }

    public List<Pupil> findByClassLevel(String classLevel) {
        List<Pupil> pupils = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_CLASS_LEVEL_SQL)) {
            statement.setString(1, classLevel);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    pupils.add(mapPupil(resultSet));
                }
            }
            return pupils;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find pupils for class level " + classLevel + ".", exception);
        }
    }

    public void update(Pupil pupil) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_SQL)) {
            statement.setString(1, pupil.getName());
            statement.setString(2, pupil.getClassLevel());
            statement.setString(3, pupil.getGuardianContact());
            statement.setInt(4, pupil.getPupilId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update pupil with ID " + pupil.getPupilId() + ".", exception);
        }
    }

    public void delete(int pupilId) {
        try (PreparedStatement statement = getConnection().prepareStatement(DELETE_SQL)) {
            statement.setInt(1, pupilId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete pupil with ID " + pupilId + ".", exception);
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

    private Pupil mapPupil(ResultSet resultSet) throws SQLException {
        return new Pupil(
                resultSet.getInt("pupil_id"),
                resultSet.getString("name"),
                resultSet.getString("class_level"),
                resultSet.getString("guardian_contact")
        );
    }
}
