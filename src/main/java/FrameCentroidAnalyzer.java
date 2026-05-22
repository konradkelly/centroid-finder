package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.util.List;

public class FrameCentroidAnalyzer {

    private final ImageBinarizer binarizer;
    private final ImageGroupFinder groupFinder;

    public FrameCentroidAnalyzer(int targetColor, int threshold) {
        this.binarizer = new DistanceImageBinarizer(
            new EuclideanColorDistance(),
            targetColor,
            threshold
        );

        this.groupFinder = new BinarizingImageGroupFinder(
            binarizer,
            new DfsBinaryGroupFinder()
        );
    }

    public TimestampedCentroidResult analyze(FrameSample sample) {

        BufferedImage frameImage = sample.frameImage();

        List<Group> groups =
            groupFinder.findConnectedGroups(frameImage);

        if (groups.isEmpty()) {
            return TimestampedCentroidResult.missing(
                sample.timestampSeconds()
            );
        }

        Group largestGroup = groups.get(0);

        return new TimestampedCentroidResult(
            sample.timestampSeconds(),
            largestGroup.centroid().x(),
            largestGroup.centroid().y()
        );
    }
}