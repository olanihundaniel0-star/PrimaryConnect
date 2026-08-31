package com.primaryconnect.service;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SyncLogDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Score;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports attendance and score data to CSV files for synchronization purposes.
 */
public class SyncExporter {
    private final AttendanceDAO attendanceDAO;
    private final ScoreDAO scoreDAO;
    private final SyncLogDAO syncLogDAO;

    public SyncExporter() {
        this.attendanceDAO = new AttendanceDAO();
        this.scoreDAO = new ScoreDAO();
        this.syncLogDAO = new SyncLogDAO();
    }

    public void export(String exportPath) {
        String deviceId;
        try {
            deviceId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            deviceId = "unknown-device";
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        try {
            List<AttendanceRecord> attendanceRecords = attendanceDAO.findAll();
            List<Score> scores = scoreDAO.findAll();

            exportAttendance(exportPath, attendanceRecords);
            exportScores(exportPath, scores);

            int totalRecords = attendanceRecords.size() + scores.size();
            syncLogDAO.create(deviceId, timestamp, totalRecords, "SUCCESS");

        } catch (Exception exception) {
            syncLogDAO.create(deviceId, timestamp, 0, "FAILED");
            throw new RuntimeException("Export failed.", exception);
        }
    }

    private void exportAttendance(String exportPath, List<AttendanceRecord> records) throws IOException {
        Path filePath = Path.of(exportPath, "attendance_export.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("attendance_id,pupil_id,date,status");
            writer.newLine();

            for (AttendanceRecord record : records) {
                writer.write(String.format("%d,%d,%s,%s",
                        record.getAttendanceId(),
                        record.getPupilId(),
                        record.getDate(),
                        record.getStatus()));
                writer.newLine();
            }
        }
    }

    private void exportScores(String exportPath, List<Score> scores) throws IOException {
        Path filePath = Path.of(exportPath, "scores_export.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("score_id,pupil_id,subject_id,session,term,test_score,exam_score,final_score,grade");
            writer.newLine();

            for (Score score : scores) {
                writer.write(String.format("%d,%d,%d,%s,%s,%.2f,%.2f,%.2f,%s",
                        score.getScoreId(),
                        score.getPupilId(),
                        score.getSubjectId(),
                        score.getSession(),
                        score.getTerm(),
                        score.getTestScore(),
                        score.getExamScore(),
                        score.getFinalScore(),
                        score.getGrade()));
                writer.newLine();
            }
        }
    }
}
