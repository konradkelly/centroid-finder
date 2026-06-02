package io.github.konradkelly.centroidfinder;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

public class DistanceImageBinarizerTest {

    @Test
    public void testAllPixelsBecomeWhite() {
        ColorDistanceFinder fake = (a, b) -> 0;

        DistanceImageBinarizer bin =
            new DistanceImageBinarizer(fake, 0x000000, 10);

        BufferedImage img =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        int[][] result = bin.toBinaryArray(img);

        assertEquals(1, result[0][0]);
        assertEquals(1, result[1][1]);
    }

    @Test
    public void testAllPixelsBecomeBlack() {
        ColorDistanceFinder fake = (a, b) -> 1000;

        DistanceImageBinarizer bin =
            new DistanceImageBinarizer(fake, 0x000000, 10);

        BufferedImage img =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        int[][] result = bin.toBinaryArray(img);

        assertEquals(0, result[0][0]);
        assertEquals(0, result[1][1]);
    }

    @Test
    public void testToBufferedImage() {
        DistanceImageBinarizer bin =
            new DistanceImageBinarizer((a,b)->0, 0, 10);

        int[][] input = {
            {1, 0},
            {0, 1}
        };

        BufferedImage img = bin.toBufferedImage(input);

        assertEquals(0xFFFFFF, img.getRGB(0,0) & 0xFFFFFF);
        assertEquals(0x000000, img.getRGB(1,0) & 0xFFFFFF);
    }

    @Test
    public void pixelColorIsReadCorrectlyAfterBulkOptimization() {
        // Verifies that each pixel's exact RGB value is used — not row/col swapped
        // and not affected by any bulk-read indexing error.
        // Pixel at (x=1, y=0) is red (0xFF0000); all others are black (0x000000).
        // EuclideanColorDistance: red vs red = 0 → white; red vs black = large → black.
        BufferedImage img = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        img.setRGB(1, 0, 0xFF0000); // x=1, y=0

        ColorDistanceFinder euclidean = new EuclideanColorDistance();
        DistanceImageBinarizer bin = new DistanceImageBinarizer(euclidean, 0xFF0000, 10);

        int[][] result = bin.toBinaryArray(img);

        assertEquals(1, result[0][1]); // (y=0, x=1) → white (distance 0)
        assertEquals(0, result[0][0]); // (y=0, x=0) → black
        assertEquals(0, result[1][0]); // (y=1, x=0) → black
    }
}