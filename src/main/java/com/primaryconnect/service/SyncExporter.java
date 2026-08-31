package com.primaryconnect.service;

import com.primaryconnect.data.DatabaseManager;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Score;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports local records to a file on a removable USB device for offline transfer to another laptop.
 */
public class SyncExporter {
    public SyncExporter() {
    }

    public void export(String exportPath) {
        Path exportDirectory = Path.of(exportPath);
        try {
            Files.createDirectories(exportDirectory);

            List<AttendanceRecord> attendanceRecords = loadAttendanceRecords();
            List<Score> scores = loadScores();

            writeAttendanceFile(exportDirectory.resolve("attendance_export.csv"), attendanceRecords);
            writeScoresFile(exportDirectory.resolve("scores_export.csv"), scores);

            System.out.println("Exported " + attendanceRecords.size() + " attendance records and "
                    + scores.size() + " score records to " + exportDirectory.toAbsolutePath() + ".");
        } catch (IOException | SQLException exception) {
            throw new RuntimeException("Export failed for path " + exportPath + ".", exception);
        }
    }

    private List<AttendanceRecord> loadAttendanceRecords() throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = """
                SELECT attendance_id, pupil_id, date, status
                FROM attendance
                ORDER BY attendance_id
                """;

        Connection connection = DatabaseManager.getInstance().getConnection();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                records.add(new AttendanceRecord(
                        resultSet.getInt("attendance_id"),
                        resultSet.getInt("pupil_id"),
                        LocalDate.parse(resultSet.getString("date")),
                        resultSet.getString("status")
                ));
            }
        }

        return records;
    }

    private List<Score> loadScores() throws SQLException {
        List<Score> scores = new ArrayList<>();
        String sql = """
                SELECT score_id, pupil_id, subject_id, session, term, test_score, exam_score, final_score, grade
                FROM scores
                ORDER BY score_id
                """;

        Connection connection = DatabaseManager.getInstance().getConnection();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                scores.add(new Score(
                        resultSet.getInt("score_id"),
                        resultSet.getInt("pupil_id"),
                        resultSet.getInt("subject_id"),
                        resultSet.getString("session"),
                        resultSet.getString("term"),
                        resultSet.getDouble("test_score"),
                        resultSet.getDouble("exam_score"),
                        resultSet.getDouble("final_score"),
                        resultSet.getString("grade")
                ));
            }
        }

        return scores;
    }

    private void writeAttendanceFile(Path filePath, List<AttendanceRecord> records) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("attendance_id,pupil_id,date,status");
            writer.newLine();
            for (AttendanceRecord record : records) {
                writer.write(record.getAttendanceId() + "," + record.getPupilId() + "," + record.getDate() + "," + record.getStatus());
                writer.newLine();
            }
        }
    }

    private void writeScoresFile(Path filePath, List<Score> scores) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("score_id,pupil_id,subject_id,session,term,test_score,exam_score,final_score,grade");
            writer.newLine();
            for (Score score : scores) {
                writer.write(score.getScoreId() + "," + score.getPupilId() + "," + score.getSubjectId() + ","
                        + score.getSession() + "," + score.getTerm() + "," + score.getTestScore() + ","
                        + score.getExamScore() + "," + score.getFinalScore() + "," + score.getGrade());
                writer.newLine();
            }
        }
    }
}
