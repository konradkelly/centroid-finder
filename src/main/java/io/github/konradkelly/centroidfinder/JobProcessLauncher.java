package io.github.konradkelly.centroidfinder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public interface JobProcessLauncher {
    JobProcessResult launch(
        Path processorJar,
        Path inputVideo,
        Path outputPath,
        String targetColor,
        int threshold,
        Duration timeout
    ) throws IOException, InterruptedException;
}
