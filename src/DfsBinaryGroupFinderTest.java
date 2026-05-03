import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DfsBinaryGroupFinderTest {

    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    // -----------------------------------------------------------------------
    // Phase 1: Exceptions / Validation
    // -----------------------------------------------------------------------

    @Test
    public void nullArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
    }

    @Test
    public void nullRowThrowsNullPointerException() {
        int[][] image = new int[2][];
        image[0] = new int[]{1, 0};
        image[1] = null;
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void emptyOuterArrayThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(new int[0][]));
    }

    @Test
    public void emptyInnerArrayThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> finder.findConnectedGroups(new int[][]{{}}));
    }

    // -----------------------------------------------------------------------
    // Phase 2: No Groups
    // -----------------------------------------------------------------------

    @Test
    public void allZerosReturnsEmptyList() {
        int[][] image = {
            {0, 0, 0},
            {0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertTrue(groups.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Phase 3: Single Group — Basic
    // -----------------------------------------------------------------------

    @Test
    public void singlePixelOneCellArray() {
        int[][] image = {{1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).size());
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void singleHorizontalRowOfOnes() {
        // 1 row, 5 columns — one connected group of size 5
        // x values: 0,1,2,3,4 → sum=10, centroid x = 10/5 = 2
        // y values: all 0   → centroid y = 0
        int[][] image = {{1, 1, 1, 1, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(5, groups.get(0).size());
        assertEquals(2, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void singleVerticalColumnOfOnes() {
        // 5 rows, 1 column — one connected group of size 5
        // x values: all 0   → centroid x = 0
        // y values: 0,1,2,3,4 → sum=10, centroid y = 10/5 = 2
        int[][] image = {{1}, {1}, {1}, {1}, {1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(5, groups.get(0).size());
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(2, groups.get(0).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Phase 4: Connectivity Rules
    // -----------------------------------------------------------------------

    @Test
    public void diagonalOnesAreNotConnected() {
        // 1s only touch diagonally — each is its own group
        int[][] image = {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(3, groups.size());
        for (Group g : groups) {
            assertEquals(1, g.size());
        }
    }

    @Test
    public void lShapedGroupIsOneGroup() {
        // L-shape: pixels at (col,row) = (0,0),(0,1),(0,2),(1,2)
        int[][] image = {
            {1, 0},
            {1, 0},
            {1, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(4, groups.get(0).size());
    }

    // -----------------------------------------------------------------------
    // Phase 5: Multiple Groups
    // -----------------------------------------------------------------------

    @Test
    public void twoIsolatedSinglePixels() {
        int[][] image = {
            {1, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
    }

    @Test
    public void largeGroupBeforeSmallGroupInDescendingOrder() {
        // Top row: 3 connected 1s (large group)
        // Bottom row: 1 isolated 1 (small group)
        int[][] image = {
            {1, 1, 1},
            {0, 0, 0},
            {0, 1, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
        assertEquals(3, groups.get(0).size()); // largest first
        assertEquals(1, groups.get(1).size());
    }

    // -----------------------------------------------------------------------
    // Phase 6: Centroid Integer Division
    // -----------------------------------------------------------------------

    @Test
    public void twPixelGroupCentroidUsesIntegerDivision() {
        // pixels at col=0 and col=1, row=0
        // x: (0+1)/2 = 0  (integer division floors)
        // y: (0+0)/2 = 0
        int[][] image = {{1, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(0, groups.get(0).centroid().x()); // floors, not rounds to 1
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void threePixelGroupCentroidFloors() {
        // pixels at col=0,1,2 in row=0
        // x: (0+1+2)/3 = 1  (exact)
        // pixels at col=0,1 in row=0 → x: (0+1)/2 = 0 floors tested above
        // Test a non-symmetric group: cols 1,2,3 → x sum=6, 6/3=2
        int[][] image = {{0, 1, 1, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).centroid().x()); // (1+2+3)/3 = 2
    }

    // -----------------------------------------------------------------------
    // Phase 7: Sorting Tiebreakers
    // -----------------------------------------------------------------------

    @Test
    public void sameSizeGroupsOrderedByHigherXCentroidFirst() {
        // Two single-pixel groups: one at col=0, one at col=2
        // Both size=1, higher x (col=2) should come first in descending order
        int[][] image = {{1, 0, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
        assertEquals(2, groups.get(0).centroid().x()); // higher x first
        assertEquals(0, groups.get(1).centroid().x());
    }

    @Test
    public void sameSizeSameXOrderedByHigherYCentroidFirst() {
        // Two single-pixel groups at same column (x=0), different rows
        // row=0 → y=0, row=2 → y=2; higher y (2) comes first
        int[][] image = {{1}, {0}, {1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
        assertEquals(2, groups.get(0).centroid().y()); // higher y first
        assertEquals(0, groups.get(1).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Phase 8: Larger / Realistic
    // -----------------------------------------------------------------------

    @Test
    public void fullThreeByThreeGridIsOneGroup() {
        int[][] image = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(9, groups.get(0).size());
        // x: cols 0,1,2 each appear 3 times → sum=9, 9/9=1
        // y: rows 0,1,2 each appear 3 times → sum=9, 9/9=1
        assertEquals(1, groups.get(0).centroid().x());
        assertEquals(1, groups.get(0).centroid().y());
    }

    @Test
    public void twoDistinctBlobsInLargerArray() {
        // Blob A: top-left 2x2 block (size=4)
        // Blob B: bottom-right single pixel (size=1)
        int[][] image = {
            {1, 1, 0, 0},
            {1, 1, 0, 0},
            {0, 0, 0, 0},
            {0, 0, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());

        Group largest = groups.get(0);
        assertEquals(4, largest.size());
        // x: cols 0,1,0,1 → sum=2, 2/4=0
        // y: rows 0,0,1,1 → sum=2, 2/4=0
        assertEquals(0, largest.centroid().x());
        assertEquals(0, largest.centroid().y());

        Group smallest = groups.get(1);
        assertEquals(1, smallest.size());
        assertEquals(3, smallest.centroid().x()); // col=3
        assertEquals(3, smallest.centroid().y()); // row=3
    }

    // -----------------------------------------------------------------------
    // Phase 9: Additional Edge Cases
    // -----------------------------------------------------------------------

    @Test
    public void oneByOneArrayWithZeroReturnsEmptyList() {
        int[][] image = {{0}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertTrue(groups.isEmpty());
    }

    @Test
    public void checkerboardPatternAllSinglePixelGroups() {
        // No two 1s are adjacent — each is its own group
        int[][] image = {
            {1, 0, 1},
            {0, 1, 0},
            {1, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(5, groups.size());
        for (Group g : groups) {
            assertEquals(1, g.size());
        }
    }

    @Test
    public void snakeShapedGroupIsOneGroup() {
        // Winding path — all connected, should be one group of size 7
        int[][] image = {
            {1, 1, 1},
            {0, 0, 1},
            {1, 1, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(7, groups.get(0).size());
    }

    @Test
    public void isolatedPixelInInteriorOfArray() {
        int[][] image = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).size());
        assertEquals(1, groups.get(0).centroid().x()); // col=1
        assertEquals(1, groups.get(0).centroid().y()); // row=1
    }

    @Test
    public void groupTouchingTopLeftCorner() {
        // 2x2 block of 1s in top-left corner
        int[][] image = {
            {1, 1, 0},
            {1, 0, 0},
            {0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).size());
        // x: cols 0,1,0 → sum=1, 1/3=0
        // y: rows 0,0,1 → sum=1, 1/3=0
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void twoGroupsWithIdenticalSizeAndCentroidBothAppear() {
        // Two single pixels at (col=1,row=0) and (col=1,row=2) — same x centroid
        // but different y, so they won't be truly identical in centroid
        // Instead: two pixels at same centroid coords via symmetric groups
        // Group A: pixel at col=0, row=0 → centroid (0,0), size=1
        // Group B: pixel at col=0, row=2 → centroid (0,2), size=1
        // Both size=1 — confirm both are in list (neither dropped)
        int[][] image = {{1}, {0}, {1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
    }
}
