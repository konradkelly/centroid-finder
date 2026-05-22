package io.github.konradkelly.centroidfinder;

public class CliArgumentParser {
    public VideoProcessingConfig parse(String[] args) {
        if (args == null || args.length != 4) {
            throw new IllegalArgumentException("Usage: java -jar videoprocessor.jar inputPath outputCsv targetColor threshold");
        }

        String inputPath = args[0];
        String outputCsvPath = args[1];
        int targetColor;
        int threshold;

        try {
            targetColor = Integer.parseInt(args[2], 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("targetColor must be a hex RGB value in RRGGBB format", exception);
        }

        try {
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("threshold must be an integer", exception);
        }

        return new VideoProcessingConfig(inputPath, outputCsvPath, targetColor, threshold);
    }
}
