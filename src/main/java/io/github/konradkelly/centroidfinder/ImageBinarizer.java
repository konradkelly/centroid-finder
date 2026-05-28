package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;

/**
 * An interface for converting between RGB images and binary (black-and-white) images.
 */
public interface ImageBinarizer {
    public int[][] toBinaryArray(BufferedImage image);
    public BufferedImage toBufferedImage(int[][] image);
}