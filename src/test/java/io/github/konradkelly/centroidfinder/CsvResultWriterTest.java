package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class CsvResultWriterTest {
    @Test
    public void writesTimestampedRows() throws Exception {
        Path tempFile = Files.createTempFile("centroid", ".csv");
        try (CsvResultWriter writer = new CsvResultWriter(tempFile.toString())) {
            writer.write(new TimestampedCentroidResult(1.5, 4, 7));
        }
        assertEquals("1.5,4,7", Files.readString(tempFile).trim());
    }

    @Test
    public void writesIntegerTimestampWithoutDecimalSuffix() throws Exception {
        Path tempFile = Files.createTempFile("centroid-int", ".csv");
        try (CsvResultWriter writer = new CsvResultWriter(tempFile.toString())) {
            writer.write(new TimestampedCentroidResult(2.0, 1, 9));
        }
        assertEquals("2,1,9", Files.readString(tempFile).trim());
    }

    @Test
    public void writesMultipleRowsInOrder() throws Exception {
        Path tempFile = Files.createTempFile("centroid-multi", ".csv");
        try (CsvResultWriter writer = new CsvResultWriter(tempFile.toString())) {
            writer.write(new TimestampedCentroidResult(0.0, -1, -1));
            writer.write(new TimestampedCentroidResult(0.5, 10, 12));
        }
        assertEquals("0,-1,-1\n0.5,10,12", Files.readString(tempFile).replace("\r\n", "\n").trim());
    }

    @Test
    public void constructorRejectsInvalidOutputPath() {
        Path impossiblePath = Path.of("this", "path", "does", "not", "exist", "results.csv");
        assertThrows(IllegalArgumentException.class, () -> new CsvResultWriter(impossiblePath.toString()));
    }
}