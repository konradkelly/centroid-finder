package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class JavaCvVideoFrameReaderTest {
    private static final String SAMPLE_VIDEO_PATH = "sampleInput/blue_circle_red_bg.mp4";

    @Test
    public void constructorRejectsMissingVideoFile() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new JavaCvVideoFrameReader("definitely-not-a-real-video-file.mp4")
        );

        assertEquals("Unable to open video: definitely-not-a-real-video-file.mp4", exception.getMessage());
    }

    @Test
    public void constructorRejectsNullInputPath() {
        assertThrows(NullPointerException.class, () -> new JavaCvVideoFrameReader(null));
    }

    @Test
    public void constructorAcceptsExistingImageFileAndReturnsFrame() {
        try (JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader("sampleInput/squares.jpg")) {
            FrameSample sample = reader.nextFrame();

            assertNotNull(sample);
            assertNotNull(sample.frameImage());
            assertEquals(0.0, sample.timestampSeconds(), 1e-9);
        }
    }

    @Test
    public void nextFrameReturnsFirstFrameWithImageAndZeroTimestamp() {
        try (JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH)) {
            FrameSample first = reader.nextFrame();

            assertNotNull(first);
            assertNotNull(first.frameImage());
            assertEquals(0.0, first.timestampSeconds(), 1e-9);
        }
    }

    @Test
    public void nextFrameTimestampsIncreaseMonotonically() {
        try (JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH)) {
            List<Double> timestamps = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                FrameSample sample = reader.nextFrame();
                if (sample == null) {
                    break;
                }
                timestamps.add(sample.timestampSeconds());
            }

            assertTrue(timestamps.size() >= 2);
            for (int i = 1; i < timestamps.size(); i++) {
                assertTrue(timestamps.get(i) > timestamps.get(i - 1));
            }
        }
    }

    @Test
    public void nextFrameEventuallyReturnsNullAtEndOfStream() {
        try (JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH)) {
            FrameSample sample;
            int frameCount = 0;

            while ((sample = reader.nextFrame()) != null && frameCount < 10000) {
                frameCount++;
            }

            assertTrue(frameCount > 0);
            assertTrue(sample == null);
        }
    }

    @Test
    public void closeAfterReadDoesNotThrow() {
        JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH);
        reader.nextFrame();

        assertDoesNotThrow(reader::close);
    }

    @Test
    public void nextFrameAfterCloseThrowsIllegalStateException() {
        JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH);
        reader.close();

        IllegalStateException exception = assertThrows(IllegalStateException.class, reader::nextFrame);
        assertTrue(exception.getMessage().contains("Unable to read next video frame from:"));
    }

    @Test
    public void closeCanBeCalledTwiceWithoutThrowing() {
        JavaCvVideoFrameReader reader = new JavaCvVideoFrameReader(SAMPLE_VIDEO_PATH);

        assertDoesNotThrow(reader::close);
        assertDoesNotThrow(reader::close);
    }
}
