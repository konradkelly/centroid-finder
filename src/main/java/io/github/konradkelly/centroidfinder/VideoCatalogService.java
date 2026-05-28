package io.github.konradkelly.centroidfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class VideoCatalogService {
    private final ServerPathsProperties paths;

    public VideoCatalogService(ServerPathsProperties paths) {
        this.paths = paths;
    }

    public List<String> listVideos() {
        Path videosDir = paths.videosDir();
        try (Stream<Path> stream = Files.list(videosDir)) {
            return stream
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .sorted(Comparator.naturalOrder())
                .toList();
        } catch (IOException exception) {
            throw new ServerException("Error reading video directory", exception);
        }
    }
}