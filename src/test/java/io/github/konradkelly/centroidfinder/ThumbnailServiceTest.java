package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class ThumbnailServiceTest {
    @Test
    public void resolveVideoPathRejectsTraversalAttempts() {
        ServerPathsProperties properties = new ServerPathsProperties(
            Path.of("videos"),
            Path.of("results"),
            Path.of("processor.jar"),
            java.time.Duration.ofMinutes(10)
        );
        ThumbnailService service = new ThumbnailService(properties);
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> service.resolveVideoPath("../secret.mp4")
        );
        assertEquals("Invalid filename", exception.getMessage());
    }

    @Test
    public void resolveVideoPathRejectsMissingFiles() {
        ServerPathsProperties properties = new ServerPathsProperties(
            Path.of("videos"),
            Path.of("results"),
            Path.of("processor.jar"),
            java.time.Duration.ofMinutes(10)
        );
        ThumbnailService service = new ThumbnailService(properties);
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> service.resolveVideoPath("missing.mp4")
        );
        assertEquals("Video not found", exception.getMessage());
    }
}
