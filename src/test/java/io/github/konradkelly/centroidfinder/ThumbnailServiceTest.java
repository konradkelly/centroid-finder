package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ThumbnailServiceTest {

    @TempDir
    Path tempDir;

    private ServerPathsProperties testProperties(Path videosDir) {
        return new ServerPathsProperties(
            videosDir,
            Path.of("results"),
            Path.of("processor.jar"),
            java.time.Duration.ofMinutes(10)
        );
    }

    @Test
    public void resolveVideoPathRejectsTraversalAttempts() {
        VideoFrameReaderFactory factory = mock(VideoFrameReaderFactory.class);
        ThumbnailService service = new ThumbnailService(testProperties(Path.of("videos")), factory);
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> service.resolveVideoPath("../secret.mp4")
        );
        assertEquals("Invalid filename", exception.getMessage());
    }

    @Test
    public void resolveVideoPathRejectsMissingFiles() {
        VideoFrameReaderFactory factory = mock(VideoFrameReaderFactory.class);
        ThumbnailService service = new ThumbnailService(testProperties(Path.of("videos")), factory);
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> service.resolveVideoPath("missing.mp4")
        );
        assertEquals("Video not found", exception.getMessage());
    }

    @Test
    public void generateThumbnailReturnsBytesFromFirstFrame() throws Exception {
        Path videoFile = tempDir.resolve("test.mp4");
        Files.createFile(videoFile);

        BufferedImage frame = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        FrameSample sample = new FrameSample(0.0, frame);

        VideoFrameReader reader = mock(VideoFrameReader.class);
        when(reader.nextFrame()).thenReturn(sample);

        VideoFrameReaderFactory factory = mock(VideoFrameReaderFactory.class);
        when(factory.open(anyString())).thenReturn(reader);

        ThumbnailService service = new ThumbnailService(testProperties(tempDir), factory);

        byte[] result = service.generateThumbnail("test.mp4");

        assertNotNull(result);
        assert result.length > 0;
    }

    @Test
    public void generateThumbnailThrowsServerExceptionWhenReaderReturnsNull() throws Exception {
        Path videoFile = tempDir.resolve("empty.mp4");
        Files.createFile(videoFile);

        VideoFrameReader reader = mock(VideoFrameReader.class);
        when(reader.nextFrame()).thenReturn(null);

        VideoFrameReaderFactory factory = mock(VideoFrameReaderFactory.class);
        when(factory.open(anyString())).thenReturn(reader);

        ThumbnailService service = new ThumbnailService(testProperties(tempDir), factory);

        assertThrows(ServerException.class, () -> service.generateThumbnail("empty.mp4"));
    }

    @Test
    public void generateThumbnailThrowsServerExceptionWhenFactoryThrowsIOException() throws Exception {
        Path videoFile = tempDir.resolve("bad.mp4");
        Files.createFile(videoFile);

        VideoFrameReaderFactory factory = mock(VideoFrameReaderFactory.class);
        when(factory.open(anyString())).thenThrow(new IOException("cannot open"));

        ThumbnailService service = new ThumbnailService(testProperties(tempDir), factory);

        assertThrows(ServerException.class, () -> service.generateThumbnail("bad.mp4"));
    }
}
