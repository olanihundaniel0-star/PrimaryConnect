package com.primaryconnect.service;

import com.primaryconnect.data.DatabaseManager;
import com.primaryconnect.model.DashboardStats;
import com.primaryconnect.model.User;
import com.primaryconnect.ui.SessionContext;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Loads the aggregate values used by the JavaFX dashboard.
 */
public class DashboardService {
    private static final String MEDIA_TOPICS_SQL = """
            SELECT subjects.name AS subject_name, topics.title, topics.media_path
            FROM topics
            INNER JOIN subjects ON subjects.subject_id = topics.subject_id
            WHERE topics.media_path IS NOT NULL AND TRIM(topics.media_path) <> ''
            ORDER BY subjects.name, topics.title
            """;

    public DashboardStats loadSnapshot() {
        Connection connection = DatabaseManager.getInstance().getConnection();

        int pupilCount = queryInt(connection, "SELECT COUNT(*) FROM pupils");
        long attendanceRecords = queryLong(connection, "SELECT COUNT(*) FROM attendance");
        long presentCount = queryLong(connection, "SELECT COUNT(*) FROM attendance WHERE status = 'PRESENT'");
        long lateCount = queryLong(connection, "SELECT COUNT(*) FROM attendance WHERE status = 'LATE'");
        long absentCount = queryLong(connection, "SELECT COUNT(*) FROM attendance WHERE status = 'ABSENT'");
        long scoreCount = queryLong(connection, "SELECT COUNT(*) FROM scores");
        double averageFinalScore = queryDouble(connection, "SELECT COALESCE(AVG(final_score), 0) FROM scores");
        double attendanceRate = attendanceRecords == 0
                ? 0.0
                : ((presentCount + lateCount) * 100.0) / attendanceRecords;

        List<DashboardStats.MediaTopicPreview> mediaTopics = loadMediaTopics(connection);
        int mediaTopicCount = mediaTopics.size();

        User currentUser = SessionContext.getInstance().getCurrentUser();
        String displayName = resolveDisplayName(currentUser);
        String roleLabel = resolveRoleLabel(currentUser);

        return new DashboardStats(
                displayName,
                roleLabel,
                pupilCount,
                mediaTopicCount,
                attendanceRecords,
                presentCount,
                lateCount,
                absentCount,
                attendanceRate,
                scoreCount,
                averageFinalScore,
                List.copyOf(mediaTopics)
        );
    }

    private List<DashboardStats.MediaTopicPreview> loadMediaTopics(Connection connection) {
        List<DashboardStats.MediaTopicPreview> mediaTopics = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(MEDIA_TOPICS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String subjectName = resultSet.getString("subject_name");
                String title = resultSet.getString("title");
                String mediaPath = resultSet.getString("media_path");
                String resolvedMediaPath = MediaPathResolver.resolve(subjectName, mediaPath);
                if (!MediaPathResolver.isUsable(subjectName, mediaPath)) {
                    continue;
                }

                mediaTopics.add(new DashboardStats.MediaTopicPreview(
                        subjectName,
                        title,
                        resolvedMediaPath,
                        classifyMedia(resolvedMediaPath),
                        extractFileName(resolvedMediaPath)
                ));
            }
            return mediaTopics;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load media topics for the dashboard.", exception);
        }
    }

    private int queryInt(Connection connection, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to run dashboard count query.", exception);
        }
    }

    private long queryLong(Connection connection, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getLong(1) : 0L;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to run dashboard count query.", exception);
        }
    }

    private double queryDouble(Connection connection, String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble(1) : 0.0;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to run dashboard aggregate query.", exception);
        }
    }

    private String resolveDisplayName(User currentUser) {
        if (currentUser == null || currentUser.getUsername() == null || currentUser.getUsername().isBlank()) {
            return "PrimaryConnect teacher";
        }

        return currentUser.getUsername();
    }

    private String resolveRoleLabel(User currentUser) {
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole().isBlank()) {
            return "Teacher";
        }

        String normalized = currentUser.getRole().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] parts = normalized.split(" ");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                label.append(part.substring(1));
            }
        }
        return label.length() == 0 ? "Teacher" : label.toString();
    }

    private String classifyMedia(String mediaPath) {
        String fileName = extractFileName(mediaPath).toLowerCase(Locale.ROOT);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "FILE";
        }

        String extension = fileName.substring(dotIndex + 1);
        return switch (extension) {
            case "jpg", "jpeg", "png", "webp", "gif", "bmp", "tif", "tiff" -> "IMAGE";
            case "mp4", "mov", "m4v", "avi", "mkv", "webm", "mpeg", "mpg" -> "VIDEO";
            default -> "FILE";
        };
    }

    private String extractFileName(String mediaPath) {
        try {
            return Path.of(mediaPath).getFileName().toString();
        } catch (RuntimeException exception) {
            return mediaPath == null ? "" : mediaPath;
        }
    }
}
