package com.primaryconnect.service;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SyncLogDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;
import com.primaryconnect.model.Score;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Imports attendance and score data from CSV files for synchronization purposes.
 * 
 * Limitations:
 * 1. Pupils are not synced - pupil records are assumed to be already consistent across devices.
 * 2. Duplicate handling uses skip-if-exists logic, not true last-write-wins, since neither
 *    the attendance nor scores table tracks a last-modified timestamp.
 */
public class SyncImporter {
    private final AttendanceDAO attendanceDAO;
    private final ScoreDAO scoreDAO;
    private final SyncLogDAO syncLogDAO;

    public SyncImporter() {
        this.attendanceDAO = new AttendanceDAO();
        this.scoreDAO = new ScoreDAO();
        this.syncLogDAO = new SyncLogDAO();
    }

    public void importFrom(String importPath) {
        String deviceId;
        try {
            deviceId = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            deviceId = "unknown-device";
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Path attendancePath = Path.of(importPath, "attendance_export.csv");
        Path scoresPath = Path.of(importPath, "scores_export.csv");

        if (!Files.exists(attendancePath) || !Files.exists(scoresPath)) {
            syncLogDAO.create(deviceId, timestamp, 0, "FAILED");
            throw new RuntimeException("Import failed: missing attendance_export.csv or scores_export.csv at " + importPath);
        }

        try {
            int attendanceInserted = importAttendance(attendancePath);
            int scoresInserted = importScores(scoresPath);

            int totalInserted = attendanceInserted + scoresInserted;
            syncLogDAO.create(deviceId, timestamp, totalInserted, "SUCCESS");

        } catch (Exception exception) {
            syncLogDAO.create(deviceId, timestamp, 0, "FAILED");
            throw new RuntimeException("Import failed.", exception);
        }
    }

    private int importAttendance(Path filePath) throws IOException {
        int insertedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 4) {
                    continue;
                }

                int pupilId = Integer.parseInt(fields[1]);
                LocalDate date = LocalDate.parse(fields[2]);
                String status = fields[3];

                List<AttendanceRecord> existing = attendanceDAO.findByPupilAndDateRange(pupilId, date, date);
                if (existing.isEmpty()) {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setPupilId(pupilId);
                    record.setDate(date);
                    record.setStatus(status);
                    attendanceDAO.create(record);
                    insertedCount++;
                }
            }
        }

        return insertedCount;
    }

    private int importScores(Path filePath) throws IOException {
        int insertedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length < 9) {
                    continue;
                }

                int pupilId = Integer.parseInt(fields[1]);
                int subjectId = Integer.parseInt(fields[2]);
                AcademicSession session = AcademicSession.parse(fields[3]);
                AcademicTerm term = AcademicTerm.fromLabel(fields[4]);
                double testScore = Double.parseDouble(fields[5]);
                double examScore = Double.parseDouble(fields[6]);
                double finalScore = Double.parseDouble(fields[7]);
                String grade = fields[8];

                Score existing = scoreDAO.findByPupilSubjectTerm(pupilId, subjectId, session.toString(), term.getDisplayName());
                if (existing == null) {
                    Score score = new Score();
                    score.setPupilId(pupilId);
                    score.setSubjectId(subjectId);
                    score.setSession(session.toString());
                    score.setTerm(term.getDisplayName());
                    score.setTestScore(testScore);
                    score.setExamScore(examScore);
                    score.setFinalScore(finalScore);
                    score.setGrade(grade);
                    scoreDAO.create(score);
                    insertedCount++;
                }
            }
        }

        return insertedCount;
    }
}
