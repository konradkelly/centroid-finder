package io.github.konradkelly.centroidfinder;

/**
 * Defines an interface for computing the distance between two colors.
 */
public interface ColorDistanceFinder {
    /**
     * Computes the distance between two colors.
     *
     * @param colorA the first color as a 24-bit hex RGB integer
     * @param colorB the second color as a 24-bit hex RGB integer
     * @return the computed distance between the two colors
     */
    public double distance(int colorA, int colorB);
}