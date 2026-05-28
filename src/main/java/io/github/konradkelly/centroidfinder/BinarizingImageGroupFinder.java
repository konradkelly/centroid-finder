package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * An implementation of the ImageGroupFinder interface that first binarizes a given image and then finds
 * connected groups of white pixels using a BinaryGroupFinder.
 */
public class BinarizingImageGroupFinder implements ImageGroupFinder {
    private final ImageBinarizer binarizer;
    private final BinaryGroupFinder groupFinder;

    public BinarizingImageGroupFinder(ImageBinarizer binarizer, BinaryGroupFinder groupFinder) {
        this.binarizer = binarizer;
        this.groupFinder = groupFinder;
    }

    @Override
    public List<Group> findConnectedGroups(BufferedImage image) {
        int[][] binaryArray = binarizer.toBinaryArray(image);
        return groupFinder.findConnectedGroups(binaryArray);
    }
}