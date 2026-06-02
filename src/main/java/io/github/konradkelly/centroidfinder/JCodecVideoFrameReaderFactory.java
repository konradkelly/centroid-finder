package io.github.konradkelly.centroidfinder;

import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JCodecVideoFrameReaderFactory implements VideoFrameReaderFactory {
    @Override
    public VideoFrameReader open(String path) throws IOException {
        return new JCodecVideoFrameReader(path);
    }
}
