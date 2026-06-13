package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

public class JavaCvVideoFrameReader implements VideoFrameReader {
    private final FFmpegFrameGrabber grabber;
    private final Java2DFrameConverter converter;
    private final String videoPath;

    public JavaCvVideoFrameReader(String inputPath) {
        Objects.requireNonNull(inputPath, "inputPath");

        this.videoPath = inputPath;
        this.grabber = new FFmpegFrameGrabber(inputPath);
        this.converter = new Java2DFrameConverter();

        try {
            grabber.start();
        } catch (FrameGrabber.Exception exception) {
            try {
                grabber.close();
            } catch (Exception ignored) {
                // best-effort close
            }
            throw new IllegalArgumentException("Unable to open video: " + inputPath, exception);
        }
    }

    @Override
    public FrameSample nextFrame() {
        try {
            Frame frame;
            do {
                frame = grabber.grabImage();
                if (frame == null) {
                    return null;
                }
            } while (frame.image == null);

            BufferedImage image = converter.convert(frame);
            if (image == null) {
                return nextFrame();
            }

            double timestampSeconds = grabber.getTimestamp() / 1_000_000.0;
            return new FrameSample(timestampSeconds, image);
        } catch (FrameGrabber.Exception exception) {
            throw new IllegalStateException("Unable to read next video frame from: " + videoPath, exception);
        }
    }

    @Override
    public void close() {
        try {
            converter.close();
        } catch (Exception ignored) {
            // ignore converter cleanup failures
        }

        try {
            grabber.close();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to close video reader for: " + videoPath, exception);
        }
    }
}
