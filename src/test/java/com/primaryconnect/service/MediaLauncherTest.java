package com.primaryconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MediaLauncherTest {
    private final MediaLauncher mediaLauncher = new MediaLauncher();

    @Test
    void isLaunchableReturnsFalseForMissingFile() {
        Path missingFile = Path.of("this-file-should-not-exist-" + System.nanoTime() + ".mp4");

        assertFalse(mediaLauncher.isLaunchable(missingFile.toString()));
    }

    @Test
    void isLaunchableMatchesCurrentDesktopCapabilityForExistingFile(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.createTempFile(tempDir, "media-", ".txt");

        assertEquals(expectedLaunchable(mediaFile), mediaLauncher.isLaunchable(mediaFile.toString()));
    }

    private boolean expectedLaunchable(Path mediaFile) {
        if (!Files.exists(mediaFile)) {
            return false;
        }

        if (GraphicsEnvironment.isHeadless()) {
            return false;
        }

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
