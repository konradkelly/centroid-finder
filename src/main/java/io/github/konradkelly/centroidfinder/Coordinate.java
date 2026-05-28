package io.github.konradkelly.centroidfinder;

/**
 * Represents a location in an image or array.
 *
 * The top-left cell (row:0, column:0) is coordinate (x:0, y:0).
 * Y increases downward and X increases to the right.
 */
public record Coordinate(int x, int y) {}