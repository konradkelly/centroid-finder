package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class ThumbnailServiceTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger serviceLogger;

    @BeforeEach
    public void setUp() {
        serviceLogger = (Logger) LoggerFactory.getLogger(ThumbnailService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        serviceLogger.addAppender(listAppender);
    }

    @AfterEach
    public void tearDown() {
        serviceLogger.detachAppender(listAppender);
    }
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

    @Test
    public void generateThumbnailLogsOriginalExceptionWhenReadFails() throws IOException {
        Path tempDir = Files.createTempDirectory("thumbnail-test");
        Path fakeVideo = tempDir.resolve("fake.mp4");
        Files.writeString(fakeVideo, "not a real video");

        ServerPathsProperties properties = new ServerPathsProperties(
            tempDir,
            Path.of("results"),
            Path.of("processor.jar"),
            Duration.ofMinutes(10)
        );
        ThumbnailService service = new ThumbnailService(properties);

        assertThrows(ServerException.class, () -> service.generateThumbnail("fake.mp4"));

        boolean errorLogged = listAppender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getThrowableProxy() != null);
        assertTrue(errorLogged, "generateThumbnail should log the original exception at ERROR level before rethrowing");
    }
}
