package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class VideoCentroidPipelineTest {
    @Test
    public void runWritesResultsForFrames() throws Exception {
        // create two simple frames each with a single matching pixel
        BufferedImage img1 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        img1.setRGB(1, 1, new Color(255, 0, 0).getRGB());
        BufferedImage img2 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        img2.setRGB(2, 2, new Color(255, 0, 0).getRGB());

        VideoFrameReader reader = new VideoFrameReader() {
            private final AtomicInteger cnt = new AtomicInteger();

            @Override
            public FrameSample nextFrame() {
                int i = cnt.getAndIncrement();
                if (i == 0) return new FrameSample(0.0, img1);
                if (i == 1) return new FrameSample(1.0, img2);
                return null;
            }

            @Override
            public void close() {}
        };

        VideoFrameReaderFactory readerFactory = path -> reader;

        Path tempCsv = Files.createTempFile("out", ".csv");
        CsvResultWriterFactory writerFactory = output -> new CsvResultWriter(output);

        VideoCentroidPipeline pipeline = new VideoCentroidPipeline(readerFactory, writerFactory);

        VideoProcessingConfig config = new VideoProcessingConfig("in", tempCsv.toString(), 0, 100);
        pipeline.run(config);

        String content = Files.readString(tempCsv);
        // should contain two lines with coordinates
        assertTrue(content.lines().count() >= 2);
    }
}
