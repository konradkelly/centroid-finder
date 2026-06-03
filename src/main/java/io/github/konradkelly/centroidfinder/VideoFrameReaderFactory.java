package io.github.konradkelly.centroidfinder;

public interface VideoFrameReaderFactory {
    VideoFrameReader open(String path);

    static VideoFrameReaderFactory forJCodec() {
        return JCodecVideoFrameReader::new;
    }
}
