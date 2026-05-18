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
}