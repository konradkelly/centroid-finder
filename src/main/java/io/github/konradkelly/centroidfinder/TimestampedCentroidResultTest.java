package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TimestampedCentroidResultTest {

    @Test
    void missingCreatesNegativeCoordinates() {

        TimestampedCentroidResult result =
            TimestampedCentroidResult.missing(3.5);

        assertEquals(3.5, result.timestampSeconds());
        assertEquals(-1, result.x());
        assertEquals(-1, result.y());
    }
    @Test
void storesValuesCorrectly() {

    TimestampedCentroidResult result =
        new TimestampedCentroidResult(7.25, 100, 200);

    assertEquals(7.25, result.timestampSeconds());
    assertEquals(100, result.x());
    assertEquals(200, result.y());
    }
    @Test
void missingKeepsTimestamp() {

    TimestampedCentroidResult result =
        TimestampedCentroidResult.missing(12.75);

    assertEquals(12.75, result.timestampSeconds());
    }
}