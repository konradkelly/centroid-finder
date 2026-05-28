package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;

/**
 * An implementation of the ImageBinarizer interface that uses color distance
 * to determine whether each pixel should be black or white in the binary image.
 *
 * A pixel is white (1) if its distance to the target color is less than the threshold;
 * otherwise it is black (0).
 */
public class DistanceImageBinarizer implements ImageBinarizer {
    private final ColorDistanceFinder distanceFinder;
    private final int threshold;
    private final int targetColor;

    public DistanceImageBinarizer(ColorDistanceFinder distanceFinder, int targetColor, int threshold) {
        this.distanceFinder = distanceFinder;
        this.targetColor = targetColor;
        this.threshold = threshold;
    }

    @Override
    public int[][] toBinaryArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelColor = image.getRGB(x, y) & 0xFFFFFF;
                double dist = distanceFinder.distance(pixelColor, targetColor);
                result[y][x] = (dist < threshold) ? 1 : 0;
            }
        }

        return result;
    }

    @Override
    public BufferedImage toBufferedImage(int[][] image) {
        int height = image.length;
        int width = image[0].length;
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result.setRGB(x, y, image[y][x] == 1 ? 0xFFFFFF : 0x000000);
            }
        }

        return result;
    }
}