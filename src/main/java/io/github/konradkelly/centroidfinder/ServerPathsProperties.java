package io.github.konradkelly.centroidfinder;

import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record ServerPathsProperties(
    Path videosDir,
    Path resultsDir,
    Path videoProcessorJar,
    @DefaultValue("10m") Duration jobTimeout
) {}
