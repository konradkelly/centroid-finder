package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

public class CsvResultWriterTest {

    @Test
    void writesWholeNumberTimestampCorrectly() throws IOException {

        File tempFile = File.createTempFile("test", ".csv");

        CsvResultWriter writer =
            new CsvResultWriter(tempFile.getAbsolutePath());

        writer.write(
            new TimestampedCentroidResult(5.0, 10, 20)
        );

        writer.close();

        String content =
            Files.readString(tempFile.toPath()).trim();

        assertEquals("5,10,20", content);
    }

    @Test
    void writesDecimalTimestampCorrectly() throws IOException {

        File tempFile = File.createTempFile("test", ".csv");

        CsvResultWriter writer =
            new CsvResultWriter(tempFile.getAbsolutePath());

        writer.write(
            new TimestampedCentroidResult(5.5, 10, 20)
        );

        writer.close();

        String content =
            Files.readString(tempFile.toPath()).trim();

        assertEquals("5.5,10,20", content);
    }

    @Test
    void writeThrowsWhenPrintWriterEncountersError() {
        PrintWriter brokenWriter = new PrintWriter(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public void flush() throws IOException {
                throw new IOException("disk full");
            }

            @Override
            public void close() throws IOException {
                throw new IOException("disk full");
            }
        });

        CsvResultWriter writer = new CsvResultWriter(brokenWriter);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> writer.write(new TimestampedCentroidResult(5.0, 10, 20))
        );

        assertEquals("Unable to write output CSV", exception.getMessage());
    }
}