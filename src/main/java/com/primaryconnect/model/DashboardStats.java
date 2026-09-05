package com.primaryconnect.model;

import java.util.List;

/**
 * Snapshot of the values shown on the JavaFX dashboard.
 */
public record DashboardStats(
        String displayName,
        String roleLabel,
        int pupilCount,
        int mediaTopicCount,
        long attendanceRecords,
        long presentCount,
        long lateCount,
        long absentCount,
        double attendanceRate,
        long scoreCount,
        double averageFinalScore,
        List<MediaTopicPreview> mediaTopics
) {
    /**
     * Represents one media-backed topic shown in the dashboard spotlight.
     */
    public record MediaTopicPreview(
            String subjectName,
            String title,
            String mediaPath,
            String mediaKind,
            String fileName
    ) {
    }
}
