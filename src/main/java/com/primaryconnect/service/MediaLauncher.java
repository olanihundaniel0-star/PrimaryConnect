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
        File mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            return false;
        }

        return isDesktopOpenSupported();
    }

    public void launch(String mediaPath) {
        File mediaFile = new File(mediaPath);
        if (!mediaFile.exists()) {
            System.out.println("Media file not found: " + mediaPath);
            return;
        }

        if (!isDesktopOpenSupported()) {
            System.out.println("Desktop launching not supported on this system");
            return;
        }

        try {
            Desktop.getDesktop().open(mediaFile);
        } catch (IOException exception) {
            System.out.println("Unable to open media file: " + mediaPath);
        }
    }

    private boolean isDesktopOpenSupported() {
        try {
            if (!Desktop.isDesktopSupported()) {
                return false;
            }

            return Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
        } catch (Throwable exception) {
            return false;
        }
    }
}
