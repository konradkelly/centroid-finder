package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

public class FrameSampleTest {

    @Test
    void storesValuesCorrectly() {

        BufferedImage image =
            new BufferedImage(
                10,
                10,
                BufferedImage.TYPE_INT_RGB
            );

        FrameSample sample =
            new FrameSample(2.5, image);

        assertEquals(2.5, sample.timestampSeconds());
        assertEquals(image, sample.frameImage());
    }
    @Test
void storesDifferentTimestampCorrectly() {

    BufferedImage image =
        new BufferedImage(
            5,
            5,
            BufferedImage.TYPE_INT_RGB
        );

    FrameSample sample =
        new FrameSample(9.75, image);

    assertEquals(9.75, sample.timestampSeconds());
    }
    @Test
void storesBufferedImageReference() {

    BufferedImage image =
        new BufferedImage(
            20,
            20,
            BufferedImage.TYPE_INT_RGB
        );

    FrameSample sample =
        new FrameSample(1.0, image);

    assertEquals(image, sample.frameImage());
    }
}