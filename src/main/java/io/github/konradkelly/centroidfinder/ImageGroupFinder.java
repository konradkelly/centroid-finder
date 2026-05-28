package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.List;

public interface ImageGroupFinder {
    /**
     * Finds connected groups in an image, sorted in DESCENDING order.
     */
    public List<Group> findConnectedGroups(BufferedImage image);
}