package io.github.konradkelly.centroidfinder;

public class VideoCentroidPipeline {
    private final VideoFrameReaderFactory readerFactory;
    private final CsvResultWriterFactory writerFactory;

    public VideoCentroidPipeline() {
        this.readerFactory = VideoFrameReaderFactory.forJCodec();
        this.writerFactory = CsvResultWriterFactory.forDefault();
    }

    // Visible for tests
    public VideoCentroidPipeline(VideoFrameReaderFactory readerFactory, CsvResultWriterFactory writerFactory) {
        this.readerFactory = readerFactory;
        this.writerFactory = writerFactory;
    }

    public void run(VideoProcessingConfig config) {
        FrameCentroidAnalyzer analyzer = new FrameCentroidAnalyzer(config.targetColor(), config.threshold());

        try (VideoFrameReader reader = readerFactory.open(config.inputPath());
             CsvResultWriter writer = writerFactory.open(config.outputCsvPath())) {
            FrameSample sample;
            while ((sample = reader.nextFrame()) != null) {
                writer.write(analyzer.analyze(sample));
            }
        }
    }
}
