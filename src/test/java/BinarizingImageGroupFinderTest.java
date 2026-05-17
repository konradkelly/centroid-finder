import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BinarizingImageGroupFinderTest {

    @Test
    public void testReturnsGroups() {

        ImageBinarizer binarizer = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) {
                return new int[][] {{1,0},{1,1}};
            }
            public BufferedImage toBufferedImage(int[][] image) {
                return null;
            }
        };

        BinaryGroupFinder groupFinder = img ->
            List.of(new Group(3, new Coordinate(0,0)));

        BinarizingImageGroupFinder finder =
            new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage img =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        List<Group> result = finder.findConnectedGroups(img);

        assertEquals(1, result.size());
        assertEquals(3, result.get(0).size());
    }

    @Test
    public void testEmptyGroups() {

        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{0,0},{0,0}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> List.of();

        BinarizingImageGroupFinder finder =
            new BinarizingImageGroupFinder(bin, gf);

        BufferedImage img =
            new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        assertTrue(finder.findConnectedGroups(img).isEmpty());
    }

    // -----------------------------------------------------------------------
    // Section 1: Return value basics
    // -----------------------------------------------------------------------

    @Test
    public void resultIsNeverNull() {
        // Even with an all-zero binary array, the result must be a non-null list
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{0}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> List.of();
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(bin, gf);

        assertNotNull(finder.findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)));
    }

    @Test
    public void groupSizeAndCentroidPassThroughUnchanged() {
        // Verify that size and centroid values from the group finder are not altered
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> List.of(new Group(7, new Coordinate(3, 5)));
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(bin, gf);

        List<Group> result = finder.findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(7, result.get(0).size());
        assertEquals(3, result.get(0).centroid().x());
        assertEquals(5, result.get(0).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Section 2: Delegation — image reaches the binarizer
    // -----------------------------------------------------------------------

    @Test
    public void imageIsPassedToBinarizer() {
        // The binarizer fake records the image it received; we verify it's the same object
        BufferedImage sentImage = new BufferedImage(3, 4, BufferedImage.TYPE_INT_RGB);
        BufferedImage[] captured = new BufferedImage[1];

        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) {
                captured[0] = image;
                return new int[][] {{0}};
            }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        new BinarizingImageGroupFinder(bin, img -> List.of()).findConnectedGroups(sentImage);

        assertSame(sentImage, captured[0]);
    }

    @Test
    public void imageDimensionsAreAvailableToBinarizer() {
        // Binarizer receives an image with correct dimensions (3 wide, 2 tall)
        int[] dims = new int[2];
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) {
                dims[0] = image.getWidth();
                dims[1] = image.getHeight();
                return new int[][] {{0, 0, 0}, {0, 0, 0}};
            }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        new BinarizingImageGroupFinder(bin, img -> List.of())
            .findConnectedGroups(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB));

        assertEquals(3, dims[0]);
        assertEquals(2, dims[1]);
    }

    // -----------------------------------------------------------------------
    // Section 3: Delegation — binary array reaches the group finder
    // -----------------------------------------------------------------------

    @Test
    public void binaryArrayFromBinarizerIsPassedToGroupFinder() {
        // The exact array the binarizer produces should arrive at the group finder
        int[][] expectedArray = {{1, 0}, {0, 1}};
        int[][][] captured = new int[1][][];

        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return expectedArray; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> { captured[0] = img; return List.of(); };
        new BinarizingImageGroupFinder(bin, gf)
            .findConnectedGroups(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));

        assertSame(expectedArray, captured[0]);
    }

    // -----------------------------------------------------------------------
    // Section 4: Multiple groups and ordering
    // -----------------------------------------------------------------------

    @Test
    public void multipleGroupsAreAllReturned() {
        // Group finder returns 3 groups; all 3 should appear in the result
        List<Group> fakeGroups = List.of(
            new Group(5, new Coordinate(2, 0)),
            new Group(3, new Coordinate(0, 4)),
            new Group(1, new Coordinate(1, 1))
        );
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        List<Group> result = new BinarizingImageGroupFinder(bin, img -> fakeGroups)
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(3, result.size());
    }

    @Test
    public void orderingFromGroupFinderIsPreserved() {
        // findConnectedGroups must return groups in the same order the group finder provides
        List<Group> fakeGroups = List.of(
            new Group(9, new Coordinate(4, 4)),
            new Group(2, new Coordinate(0, 0))
        );
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        List<Group> result = new BinarizingImageGroupFinder(bin, img -> fakeGroups)
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(9, result.get(0).size());
        assertEquals(2, result.get(1).size());
    }

    @Test
    public void singlePixelImageWithOneGroupIsReturned() {
        // Minimal 1x1 image with a single foreground pixel
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        List<Group> result = new BinarizingImageGroupFinder(
                bin, img -> List.of(new Group(1, new Coordinate(0, 0))))
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).centroid().x());
        assertEquals(0, result.get(0).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Section 5: Call counts and independence
    // -----------------------------------------------------------------------

    @Test
    public void binarizerIsCalledExactlyOnce() {
        // Verifies the implementation doesn't skip or double-call the binarizer
        int[] callCount = {0};
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) {
                callCount[0]++;
                return new int[][] {{0}};
            }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        new BinarizingImageGroupFinder(bin, img -> List.of())
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(1, callCount[0]);
    }

    @Test
    public void groupFinderIsCalledExactlyOnce() {
        // Verifies the implementation doesn't skip or double-call the group finder
        int[] callCount = {0};
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{0}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> { callCount[0]++; return List.of(); };
        new BinarizingImageGroupFinder(bin, gf)
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(1, callCount[0]);
    }

    @Test
    public void twoCallsToSameFinderAreIndependent() {
        // Ensures results aren't cached — second call should reflect new binarizer output
        int[] callNum = {0};
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{0}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> callNum[0]++ == 0
            ? List.of(new Group(1, new Coordinate(0, 0)))
            : List.of(new Group(1, new Coordinate(0, 0)), new Group(1, new Coordinate(1, 1)));

        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(bin, gf);
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        List<Group> firstResult = finder.findConnectedGroups(img);
        List<Group> secondResult = finder.findConnectedGroups(img);

        assertEquals(1, firstResult.size());
        assertEquals(2, secondResult.size());
    }

    @Test
    public void largeCentroidValuesPassThroughUnchanged() {
        // Verifies no clamping or math is applied to centroid coordinates
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        BinaryGroupFinder gf = img -> List.of(new Group(100, new Coordinate(999, 888)));
        List<Group> result = new BinarizingImageGroupFinder(bin, gf)
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(999, result.get(0).centroid().x());
        assertEquals(888, result.get(0).centroid().y());
    }

    @Test
    public void tenGroupsAllReturned() {
        // Verifies a large number of groups are not truncated or dropped
        List<Group> fakeGroups = List.of(
            new Group(10, new Coordinate(9, 0)),
            new Group(9,  new Coordinate(8, 0)),
            new Group(8,  new Coordinate(7, 0)),
            new Group(7,  new Coordinate(6, 0)),
            new Group(6,  new Coordinate(5, 0)),
            new Group(5,  new Coordinate(4, 0)),
            new Group(4,  new Coordinate(3, 0)),
            new Group(3,  new Coordinate(2, 0)),
            new Group(2,  new Coordinate(1, 0)),
            new Group(1,  new Coordinate(0, 0))
        );
        ImageBinarizer bin = new ImageBinarizer() {
            public int[][] toBinaryArray(BufferedImage image) { return new int[][] {{1}}; }
            public BufferedImage toBufferedImage(int[][] image) { return null; }
        };
        List<Group> result = new BinarizingImageGroupFinder(bin, img -> fakeGroups)
            .findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertEquals(10, result.size());
    }
}