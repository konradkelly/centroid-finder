package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class FrameCentroidAnalyzerTest {
    @Test
    public void analyzeReturnsLargestGroupCentroid() {
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.RED.getRGB());
        FrameCentroidAnalyzer analyzer = new FrameCentroidAnalyzer(0xFF0000, 1);
        TimestampedCentroidResult result = analyzer.analyze(new FrameSample(2.5, image));
        assertEquals(2.5, result.timestampSeconds());
        assertEquals(0, result.x());
        assertEquals(0, result.y());
    }

    @Test
    public void analyzeReturnsMissingWhenNoMatchingPixels() {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.BLUE.getRGB());
        image.setRGB(1, 0, Color.BLUE.getRGB());
        image.setRGB(0, 1, Color.BLUE.getRGB());
        image.setRGB(1, 1, Color.BLUE.getRGB());
        FrameCentroidAnalyzer analyzer = new FrameCentroidAnalyzer(0xFF0000, 1);
        TimestampedCentroidResult result = analyzer.analyze(new FrameSample(3.0, image));
        assertEquals(3.0, result.timestampSeconds());
        assertEquals(-1, result.x());
        assertEquals(-1, result.y());
    }

    @Test
    public void analyzeChoosesLargestGroupWhenMultipleGroupsExist() {
        BufferedImage image = new BufferedImage(6, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(1, 0, Color.RED.getRGB());
        image.setRGB(2, 0, Color.RED.getRGB());
        image.setRGB(5, 1, Color.RED.getRGB());
        FrameCentroidAnalyzer analyzer = new FrameCentroidAnalyzer(0xFF0000, 1);
        TimestampedCentroidResult result = analyzer.analyze(new FrameSample(4.0, image));
        assertEquals(4.0, result.timestampSeconds());
        assertEquals(1, result.x());
        assertEquals(0, result.y());
    }
}