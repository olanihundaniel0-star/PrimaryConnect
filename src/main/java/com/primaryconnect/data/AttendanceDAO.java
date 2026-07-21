package com.primaryconnect.data;

import com.primaryconnect.model.AttendanceRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access operations for attendance records.
 */
public class AttendanceDAO {
    private static final String INSERT_SQL = """
            INSERT INTO attendance (pupil_id, date, status)
            VALUES (?, ?, ?)
            """;
    private static final String FIND_BY_PUPIL_AND_DATE_RANGE_SQL = """
            SELECT attendance_id, pupil_id, date, status
            FROM attendance
            WHERE pupil_id = ? AND date >= ? AND date <= ?
            ORDER BY date, attendance_id
            """;
    private static final String UPDATE_SQL = """
            UPDATE attendance
            SET pupil_id = ?, date = ?, status = ?
            WHERE attendance_id = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM attendance
            WHERE attendance_id = ?
            """;

    public AttendanceDAO() {
    }

    public int create(AttendanceRecord record) {
        try (PreparedStatement statement = getConnection().prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, record.getPupilId());
            statement.setString(2, record.getDate().toString());
            statement.setString(3, record.getStatus());
            statement.executeUpdate();

            int attendanceId = readGeneratedId(statement, "create attendance record");
            record.setAttendanceId(attendanceId);
            return attendanceId;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create attendance record.", exception);
        }
    }

    public List<AttendanceRecord> findByPupilAndDateRange(int pupilId, LocalDate start, LocalDate end) {
        List<AttendanceRecord> records = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(FIND_BY_PUPIL_AND_DATE_RANGE_SQL)) {
            statement.setInt(1, pupilId);
            statement.setString(2, start.toString());
            statement.setString(3, end.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapAttendanceRecord(resultSet));
                }
            }
            return records;
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Failed to find attendance records for pupil ID " + pupilId + " from " + start + " to " + end + ".",
                    exception
            );
        }
    }

    public void update(AttendanceRecord record) {
        try (PreparedStatement statement = getConnection().prepareStatement(UPDATE_SQL)) {
            statement.setInt(1, record.getPupilId());
            statement.setString(2, record.getDate().toString());
            statement.setString(3, record.getStatus());
            statement.setInt(4, record.getAttendanceId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update attendance record with ID " + record.getAttendanceId() + ".", exception);
        }
    }

    public void delete(int attendanceId) {
        try (PreparedStatement statement = getConnection().prepareStatement(DELETE_SQL)) {
            statement.setInt(1, attendanceId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete attendance record with ID " + attendanceId + ".", exception);
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

    private AttendanceRecord mapAttendanceRecord(ResultSet resultSet) throws SQLException {
        return new AttendanceRecord(
                resultSet.getInt("attendance_id"),
                resultSet.getInt("pupil_id"),
                LocalDate.parse(resultSet.getString("date")),
                resultSet.getString("status")
        );
    }
}
