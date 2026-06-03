package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class VideoCatalogServiceTest {
    @Test
    public void listVideosReturnsSortedFilenames() throws Exception {
        Path tempDir = Files.createTempDirectory("videos");
        try {
            Files.createFile(tempDir.resolve("b.mp4"));
            Files.createFile(tempDir.resolve("a.mp4"));

            ServerPathsProperties props = new ServerPathsProperties(
                tempDir,
                Path.of("results"),
                Path.of("processor.jar"),
                java.time.Duration.ofMinutes(10)
            );
            VideoCatalogService service = new VideoCatalogService(props);
            List<String> names = service.listVideos();
            assertEquals(List.of("a.mp4", "b.mp4"), names);
        } finally {
            Files.deleteIfExists(tempDir.resolve("a.mp4"));
            Files.deleteIfExists(tempDir.resolve("b.mp4"));
            Files.deleteIfExists(tempDir);
        }
    }

    @Test
    public void listVideosThrowsServerExceptionWhenDirectoryMissing() {
        ServerPathsProperties props = new ServerPathsProperties(
            Path.of("non-existent-dir"),
            Path.of("results"),
            Path.of("processor.jar"),
            java.time.Duration.ofMinutes(10)
        );
        VideoCatalogService service = new VideoCatalogService(props);
        assertThrows(ServerException.class, service::listVideos);
    }
}
