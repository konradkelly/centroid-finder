package io.github.konradkelly.centroidfinder;

public record TimestampedCentroidResult(
    double timestampSeconds,
    int x,
    int y
) {

    public static TimestampedCentroidResult missing(
        double timestampSeconds
    ) {
        return new TimestampedCentroidResult(
            timestampSeconds,
            -1,
            -1
        );
    }
}