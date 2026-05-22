package io.github.konradkelly.centroidfinder;

public class VideoCentroidPipeline {
    public void run(VideoProcessingConfig config) {
        FrameCentroidAnalyzer analyzer = new FrameCentroidAnalyzer(config.targetColor(), config.threshold());

        try (VideoFrameReader reader = new JCodecVideoFrameReader(config.inputPath());
             CsvResultWriter writer = new CsvResultWriter(config.outputCsvPath())) {
            FrameSample sample;
            while ((sample = reader.nextFrame()) != null) {
                writer.write(analyzer.analyze(sample));
            }
        }
    }
}
