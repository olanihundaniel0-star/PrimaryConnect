package com.primaryconnect.model;

import java.time.LocalDate;

/**
 * Represents a single attendance record for a pupil on a given day.
 */
public class AttendanceRecord {
    private int attendanceId;
    private int pupilId;
    private LocalDate date;
    private String status;

    public AttendanceRecord() {
    }

    public AttendanceRecord(int attendanceId, int pupilId, LocalDate date, String status) {
        this.attendanceId = attendanceId;
        this.pupilId = pupilId;
        this.date = date;
        this.status = status;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getPupilId() {
        return pupilId;
    }

    public void setPupilId(int pupilId) {
        this.pupilId = pupilId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
