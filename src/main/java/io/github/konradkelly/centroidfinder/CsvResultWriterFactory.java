package io.github.konradkelly.centroidfinder;

public interface CsvResultWriterFactory {
    CsvResultWriter open(String outputPath);

    static CsvResultWriterFactory forDefault() {
        return CsvResultWriter::new;
    }
}
