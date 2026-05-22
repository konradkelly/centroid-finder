package io.github.konradkelly.centroidfinder;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CsvResultWriter implements Closeable {

    private final PrintWriter writer;

    public CsvResultWriter(String outputPath) {

        try {
            this.writer = new PrintWriter(outputPath);

        } catch (FileNotFoundException exception) {

            throw new IllegalArgumentException(
                "Unable to open output CSV: " + outputPath,
                exception
            );
        }
    }

    public void write(TimestampedCentroidResult result) {

        writer.println(
            formatTimestamp(result.timestampSeconds())
            + ","
            + result.x()
            + ","
            + result.y()
        );
    }

    private String formatTimestamp(double timestampSeconds) {

        if (timestampSeconds == Math.rint(timestampSeconds)) {
            return Long.toString((long) timestampSeconds);
        }

        return Double.toString(timestampSeconds);
    }

    @Override
    public void close() {
        writer.close();
    }
}