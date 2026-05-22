package io.github.konradkelly.centroidfinder;

public record VideoProcessingConfig(String inputPath, String outputCsvPath, int targetColor, int threshold) {}
