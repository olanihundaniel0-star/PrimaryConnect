package com.primaryconnect.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaLauncherTest {
    @Test
    void isLaunchableReturnsFalseForMissingFile() {
        MediaLauncher mediaLauncher = new MediaLauncher();
        Path missingFile = Path.of("this-file-should-not-exist-" + System.nanoTime() + ".mp4");

        assertFalse(mediaLauncher.isLaunchable(missingFile.toString()));
    }

    @Test
    void isLaunchableReturnsFalseForBlankPath() {
        MediaLauncher mediaLauncher = new MediaLauncher();

        assertFalse(mediaLauncher.isLaunchable("   "));
    }

    @Test
    void launchReturnsFalseForMissingFile() {
        MediaLauncher mediaLauncher = new MediaLauncher();
        Path missingFile = Path.of("this-file-should-not-exist-" + System.nanoTime() + ".mp4");

        assertFalse(mediaLauncher.launch(missingFile.toString()));
    }

    @Test
    void launchReturnsTrueWhenDesktopLaunchSucceeds(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.createTempFile(tempDir, "media-", ".txt");
        MediaLauncher mediaLauncher = new SuccessfulMediaLauncher();

        assertTrue(mediaLauncher.launch(mediaFile.toString()));
    }

    @Test
    void launchFallsBackToBundledDemoMediaWhenOriginalPathIsMissing() {
        MediaLauncher mediaLauncher = new SuccessfulMediaLauncher();

        assertTrue(mediaLauncher.launch("Basic Science", "/home/daniel/projects/primaryconnectmedia/missing-video.mp4"));
    }

    @Test
    void launchReturnsFalseWhenDesktopLaunchIsNotSupported(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.createTempFile(tempDir, "media-", ".txt");
        MediaLauncher mediaLauncher = new UnsupportedMediaLauncher();

        assertFalse(mediaLauncher.launch(mediaFile.toString()));
    }

    @Test
    void isLaunchableReturnsTrueWhenDesktopLaunchIsSupported(@TempDir Path tempDir) throws Exception {
        Path mediaFile = Files.createTempFile(tempDir, "media-", ".txt");
        MediaLauncher mediaLauncher = new SupportedMediaLauncher();

        assertTrue(mediaLauncher.isLaunchable(mediaFile.toString()));
    }

    private static class SupportedMediaLauncher extends MediaLauncher {
        @Override
        protected boolean isDesktopOpenSupported() {
            return true;
        }

        @Override
        protected void openMedia(java.io.File mediaFile) {
            // no-op for the test
        }
    }

    private static class SuccessfulMediaLauncher extends MediaLauncher {
        @Override
        protected boolean isDesktopOpenSupported() {
            return true;
        }

        @Override
        protected void openMedia(java.io.File mediaFile) {
            // no-op for the test
        }
    }

    private static class UnsupportedMediaLauncher extends MediaLauncher {
        @Override
        protected boolean isDesktopOpenSupported() {
            return false;
        }
    }
}
