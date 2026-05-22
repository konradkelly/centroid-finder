package io.github.konradkelly.centroidfinder;

public interface VideoFrameReader extends AutoCloseable {
    FrameSample nextFrame();

    @Override
    void close();
}
