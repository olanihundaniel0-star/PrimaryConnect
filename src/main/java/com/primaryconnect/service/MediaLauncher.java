package com.primaryconnect.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * Opens linked media files for lessons through the Java Desktop API when the host platform supports it.
 */
public class MediaLauncher {
    public MediaLauncher() {
    }

    public boolean isLaunchable(String mediaPath) {
        return isLaunchable(null, mediaPath);
    }

    public boolean isLaunchable(String subjectHint, String mediaPath) {
        File mediaFile = toMediaFile(subjectHint, mediaPath);
        if (mediaFile == null || !mediaFile.exists()) {
            return false;
        }

        return isDesktopOpenSupported();
    }

    public boolean launch(String mediaPath) {
        return launch(null, mediaPath);
    }

    public boolean launch(String subjectHint, String mediaPath) {
        File mediaFile = toMediaFile(subjectHint, mediaPath);
        if (mediaFile == null || !mediaFile.exists()) {
            System.out.println("Media file not found: " + mediaPath);
            return false;
        }

        if (!isDesktopOpenSupported()) {
            System.out.println("Desktop launching not supported on this system");
            return false;
        }

        try {
            openMedia(mediaFile);
            return true;
        } catch (IOException | RuntimeException exception) {
            System.out.println("Unable to open media file: " + mediaPath);
            return false;
        }
    }

    protected boolean isDesktopOpenSupported() {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }

            return Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
        } catch (Throwable exception) {
            return false;
        }
    }

    protected void openMedia(File mediaFile) throws IOException {
        Desktop.getDesktop().open(mediaFile);
    }

    private File toMediaFile(String subjectHint, String mediaPath) {
        String resolvedMediaPath = MediaPathResolver.resolve(subjectHint, mediaPath);
        if (resolvedMediaPath == null || resolvedMediaPath.isBlank()) {
            return null;
        }

        return new File(resolvedMediaPath);
    }
}
