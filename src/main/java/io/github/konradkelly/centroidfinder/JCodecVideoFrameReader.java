package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

public class JCodecVideoFrameReader implements VideoFrameReader {
    private final File videoFile;
    private final SeekableByteChannel channel;
    private final FrameGrab frameGrab;
    private final double frameRate;
    private int frameIndex;
    private boolean closed;

    public JCodecVideoFrameReader(String inputPath) {
        this.videoFile = new File(inputPath);
        SeekableByteChannel openedChannel = null;
        try {
            openedChannel = NIOUtils.readableChannel(videoFile);
            FrameGrab createdFrameGrab = FrameGrab.createFrameGrab(openedChannel);
            double detectedFrameRate = createdFrameGrab.getVideoTrack().getMeta().getTotalDuration() > 0
                ? createdFrameGrab.getVideoTrack().getMeta().getTotalFrames() / createdFrameGrab.getVideoTrack().getMeta().getTotalDuration()
                : 0.0;
            if (detectedFrameRate <= 0) {
                throw new IllegalArgumentException("Unable to determine frame rate for video: " + inputPath);
            }

            this.channel = openedChannel;
            this.frameGrab = createdFrameGrab;
            this.frameRate = detectedFrameRate;
            this.frameIndex = 0;
        } catch (IOException | JCodecException exception) {
            if (openedChannel != null) {
                try {
                    openedChannel.close();
                } catch (IOException ignored) {
                    // Best-effort close during constructor failure.
                }
            }
            throw new IllegalArgumentException("Unable to open video: " + inputPath, exception);
        } catch (IllegalArgumentException exception) {
            if (openedChannel != null) {
                try {
                    openedChannel.close();
                } catch (IOException ignored) {
                    // Best-effort close during constructor failure.
                }
            }
            throw exception;
        }
    }

    @Override
    public FrameSample nextFrame() {
        try {
            Picture picture = frameGrab.getNativeFrame();
            if (picture == null) {
                return null;
            }

            BufferedImage image = AWTUtil.toBufferedImage(picture);
            double timestampSeconds = frameIndex / frameRate;
            frameIndex++;
            return new FrameSample(timestampSeconds, image);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read next video frame from: " + videoFile, exception);
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        try {
            channel.close();
            closed = true;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close video reader for: " + videoFile, exception);
        }
    }
}
