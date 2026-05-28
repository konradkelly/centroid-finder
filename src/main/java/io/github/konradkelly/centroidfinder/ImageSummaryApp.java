package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * The Image Summary Application.
 *
 * Usage: java ImageSummaryApp <input_image> <hex_target_color> <threshold>
 *
 * Steps:
 * 1. Load the input image.
 * 2. Parse the target color from hex string to 24-bit integer.
 * 3. Binarize the image using Euclidean color distance and threshold.
 * 4. Write binarized image to "binarized.png".
 * 5. Find connected groups of white pixels using DFS.
 * 6. Write groups to "groups.csv" as "size,x,y" rows.
 */
public class ImageSummaryApp {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ImageSummaryApp <input_image> <hex_target_color> <threshold>");
            return;
        }

        String inputImagePath = args[0];
        String hexTargetColor = args[1];
        int threshold = 0;
        try {
            threshold = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return;
        }

        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File(inputImagePath));
        } catch (Exception e) {
            System.err.println("Error loading image: " + inputImagePath);
            e.printStackTrace();
            return;
        }

        int targetColor = 0;
        try {
            targetColor = Integer.parseInt(hexTargetColor, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return;
        }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);

        int[][] binaryArray = binarizer.toBinaryArray(inputImage);
        BufferedImage binaryImage = binarizer.toBufferedImage(binaryArray);

        try {
            ImageIO.write(binaryImage, "png", new File("binarized.png"));
            System.out.println("Binarized image saved as binarized.png");
        } catch (Exception e) {
            System.err.println("Error saving binarized image.");
            e.printStackTrace();
        }

        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        List<Group> groups = groupFinder.findConnectedGroups(inputImage);

        try (PrintWriter writer = new PrintWriter("groups.csv")) {
            for (Group group : groups) {
                writer.println(group.toCsvRow());
            }
            System.out.println("Groups summary saved as groups.csv");
        } catch (Exception e) {
            System.err.println("Error writing groups.csv");
            e.printStackTrace();
        }
    }
}