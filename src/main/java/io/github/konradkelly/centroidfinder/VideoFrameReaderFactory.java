package io.github.konradkelly.centroidfinder;

import java.io.IOException;

public interface VideoFrameReaderFactory {
    VideoFrameReader open(String path) throws IOException;
}
