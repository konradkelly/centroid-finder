package io.github.konradkelly.centroidfinder;

/**
 * Represents a group of contiguous pixels in an image.
 * Groups are comparable by size, then centroid x, then centroid y.
 */
public record Group(int size, Coordinate centroid) implements Comparable<Group> {

    @Override
    public int compareTo(Group other) {
        int comp = Integer.compare(this.size(), other.size());
        if (comp != 0) {
            return comp;
        }
        comp = Integer.compare(this.centroid().x(), other.centroid().x());
        if (comp != 0) {
            return comp;
        }
        return Integer.compare(this.centroid().y(), other.centroid().y());
    }

    public String toCsvRow() {
        return String.format("%d,%d,%d", this.size(), this.centroid().x(), this.centroid().y());
    }
}