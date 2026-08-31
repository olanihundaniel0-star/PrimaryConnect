package com.primaryconnect.service;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Score;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Imports records from a USB export, merging new data while rejecting duplicate records by composite key.
 */
public class SyncImporter {
    private final AttendanceDAO attendanceDAO;
    private final ScoreDAO scoreDAO;

    public SyncImporter() {
        this.attendanceDAO = new AttendanceDAO();
        this.scoreDAO = new ScoreDAO();
    }

    public void importFrom(String importPath) {
        Path directory = Path.of(importPath);
        Path attendanceFile = directory.resolve("attendance_export.csv");
        Path scoresFile = directory.resolve("scores_export.csv");

        if (!Files.exists(attendanceFile) || !Files.exists(scoresFile)) {
            throw new RuntimeException("Import failed: missing attendance_export.csv or scores_export.csv in " + importPath + ".");
        }

        try {
            int importedAttendance = importAttendance(attendanceFile);
            int importedScores = importScores(scoresFile);

            System.out.println("Imported " + importedAttendance + " attendance records and "
                    + importedScores + " score records from " + directory.toAbsolutePath() + ".");
        } catch (IOException exception) {
            throw new RuntimeException("Import failed for path " + importPath + ".", exception);
        }
    }

    private int importAttendance(Path filePath) throws IOException {
        int insertedCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            if (reader.readLine() == null) {
                return 0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length < 4) {
                    continue;
                }

                int pupilId = Integer.parseInt(fields[1].trim());
                LocalDate date = LocalDate.parse(fields[2].trim());
                String status = fields[3].trim();

                if (attendanceDAO.findByPupilAndDateRange(pupilId, date, date).isEmpty()) {
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

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            if (reader.readLine() == null) {
                return 0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length < 9) {
                    continue;
                }

                int pupilId = Integer.parseInt(fields[1].trim());
                int subjectId = Integer.parseInt(fields[2].trim());
                String session = fields[3].trim();
                String term = fields[4].trim();
                double testScore = Double.parseDouble(fields[5].trim());
                double examScore = Double.parseDouble(fields[6].trim());
                double finalScore = Double.parseDouble(fields[7].trim());
                String grade = fields[8].trim();

                if (scoreDAO.findByPupilSubjectTerm(pupilId, subjectId, session, term) == null) {
                    Score score = new Score();
                    score.setPupilId(pupilId);
                    score.setSubjectId(subjectId);
                    score.setSession(session);
                    score.setTerm(term);
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
