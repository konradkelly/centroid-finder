package io.github.konradkelly.centroidfinder;

public class VideoProcessorApp {
    public static void main(String[] args) {
        try {
            VideoProcessingConfig config = new CliArgumentParser().parse(args);
            new VideoCentroidPipeline().run(config);
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
