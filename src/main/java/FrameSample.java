package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;

public record FrameSample(
    double timestampSeconds,
    BufferedImage frameImage
) {}