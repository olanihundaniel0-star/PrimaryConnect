package com.primaryconnect.service;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Pupil;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Records attendance and computes date-range rollups.
 * Weekly, monthly, termly, and session summaries are all the same calculation with different date ranges because
 * the schema does not encode those boundaries; the caller decides what dates define each reporting window.
 */
public class AttendanceEngine {
    private static final Set<String> ALLOWED_STATUSES = Set.of("PRESENT", "ABSENT", "LATE");

    private final AttendanceDAO attendanceDAO;
    private final PupilDAO pupilDAO;

    public AttendanceEngine() {
        this(new AttendanceDAO(), new PupilDAO());
    }

    AttendanceEngine(AttendanceDAO attendanceDAO, PupilDAO pupilDAO) {
        this.attendanceDAO = attendanceDAO;
        this.pupilDAO = pupilDAO;
    }

    public void recordAttendance(int pupilId, LocalDate date, String status) {
        validateStatus(status);

        List<AttendanceRecord> existingRecords = attendanceDAO.findByPupilAndDateRange(pupilId, date, date);
        if (!existingRecords.isEmpty()) {
            AttendanceRecord existingRecord = existingRecords.get(0);
            existingRecord.setStatus(status);
            attendanceDAO.update(existingRecord);
            return;
        }

        attendanceDAO.create(new AttendanceRecord(0, pupilId, date, status));
    }

    public double calculateAttendancePercentage(int pupilId, LocalDate start, LocalDate end) {
        List<AttendanceRecord> records = attendanceDAO.findByPupilAndDateRange(pupilId, start, end);
        if (records.isEmpty()) {
            // No attendance recorded yet; this is a normal empty-state result, not an error.
            return 0.0;
        }

        long attendedCount = records.stream()
                .filter(record -> "PRESENT".equals(record.getStatus()) || "LATE".equals(record.getStatus()))
                .count();

        return attendedCount * 100.0 / records.size();
    }

    public Map<Integer, Double> classAttendanceSummary(String classLevel, LocalDate start, LocalDate end) {
        List<Pupil> pupils = pupilDAO.findByClassLevel(classLevel);
        Map<Integer, Double> attendanceSummary = new LinkedHashMap<>();

        for (Pupil pupil : pupils) {
            attendanceSummary.put(pupil.getPupilId(), calculateAttendancePercentage(pupil.getPupilId(), start, end));
        }

        return attendanceSummary;
    }

    private void validateStatus(String status) {
        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid attendance status: " + status + ". Expected one of PRESENT, ABSENT, or LATE."
            );
        }
    }
}
