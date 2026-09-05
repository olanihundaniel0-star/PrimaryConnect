package com.primaryconnect.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Resolves media paths to an existing local file, falling back to bundled demo media when the stored path is stale.
 */
public final class MediaPathResolver {
    private static final Path MEDIA_ROOT = Path.of("content", "media");

    private MediaPathResolver() {
    }

    public static String resolve(String subjectHint, String mediaPath) {
        Path resolvedPath = resolveExistingPath(subjectHint, mediaPath);
        if (resolvedPath != null) {
            return resolvedPath.toString();
        }

        return mediaPath == null ? "" : mediaPath;
    }

    public static Path resolveExistingPath(String subjectHint, String mediaPath) {
        Path directPath = toPath(mediaPath);
        if (directPath != null && Files.exists(directPath)) {
            return directPath;
        }

        Path demoPath = resolveDemoMediaPath(subjectHint, mediaPath);
        if (demoPath != null && Files.exists(demoPath)) {
            return demoPath;
        }

        return directPath;
    }

    public static boolean isUsable(String subjectHint, String mediaPath) {
        Path resolvedPath = resolveExistingPath(subjectHint, mediaPath);
        return resolvedPath != null && Files.exists(resolvedPath);
    }

    private static Path resolveDemoMediaPath(String subjectHint, String mediaPath) {
        String folderName = resolveSubjectFolder(subjectHint, mediaPath);
        if (folderName == null) {
            return null;
        }

        return MEDIA_ROOT.resolve(folderName).resolve("demo.html");
    }

    private static String resolveSubjectFolder(String subjectHint, String mediaPath) {
        String combined = ((subjectHint == null ? "" : subjectHint) + " " + (mediaPath == null ? "" : mediaPath))
                .toLowerCase(Locale.ROOT);

        if (combined.contains("basic science") || combined.contains("science")) {
            return "basic-science";
        }

        if (combined.contains("english")) {
            return "english-studies";
        }

        if (combined.contains("math")) {
            return "mathematics";
        }

        return normalizeFolderName(subjectHint);
    }

    private static String normalizeFolderName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return normalized.isBlank() ? null : normalized;
    }

    private static Path toPath(String mediaPath) {
        if (mediaPath == null || mediaPath.isBlank()) {
            return null;
        }

        try {
            return Path.of(mediaPath);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
