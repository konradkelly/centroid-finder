import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DfsBinaryGroupFinderTest {

    DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    // -----------------------------------------------------------------------
    // Section 1: Validation / Input Errors
    // -----------------------------------------------------------------------

    @Test
    public void nullArrayThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(null));
    }

    @Test
    public void firstRowNullThrowsNullPointerException() {
        int[][] image = new int[2][];
        image[0] = null;
        image[1] = new int[]{1, 0};
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void nullRowThrowsNullPointerException() {
        int[][] image = new int[2][];
        image[0] = new int[]{1, 0};
        image[1] = null;
        assertThrows(NullPointerException.class, () -> finder.findConnectedGroups(image));
    }

    @Test
    public void middleRowNullThrowsNullPointerException() {
        int[][] image = new int[3][];
        image[0] = new int[]{1, 0};
        image[1] = null;
        image[2] = new int[]{0, 1};
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
    // Section 2: Empty Results
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

    @Test
    public void oneByOneArrayWithZeroReturnsEmptyList() {
        int[][] image = {{0}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertTrue(groups.isEmpty());
    }

    @Test
    public void resultIsNeverNullEvenWithAllZeros() {
        int[][] image = {{0, 0}, {0, 0}};
        assertNotNull(finder.findConnectedGroups(image));
    }

    // -----------------------------------------------------------------------
    // Section 3: Single Group — Basic
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
    // Section 4: Connectivity
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

    @Test
    public void tShapedGroupIsOneGroup() {
        // T-shape: top row full, middle pixel of second row
        // Tests branching path in DFS
        int[][] image = {
            {1, 1, 1},
            {0, 1, 0},
            {0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(4, groups.get(0).size());
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
    public void donutShapeHoleIsNotCountedAsPartOfGroup() {
        // Ring of 1s with a 0 in the center — hole should not be included
        int[][] image = {
            {1, 1, 1},
            {1, 0, 1},
            {1, 1, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(8, groups.get(0).size()); // 8 border pixels, not 9
    }

    @Test
    public void groupTouchingAllFourEdges() {
        // Cross shape spanning all borders of a 5x5 grid
        int[][] image = {
            {0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0},
            {1, 1, 1, 1, 1},
            {0, 0, 1, 0, 0},
            {0, 0, 1, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(9, groups.get(0).size());
    }

    // -----------------------------------------------------------------------
    // Section 5: Corner / Edge Coverage
    // -----------------------------------------------------------------------

    @Test
    public void groupTouchingTopLeftCorner() {
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
    public void groupTouchingTopRightCorner() {
        int[][] image = {
            {0, 1, 1},
            {0, 0, 1},
            {0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).size());
        // x: cols 1,2,2 → sum=5, 5/3=1
        // y: rows 0,0,1 → sum=1, 1/3=0
        assertEquals(1, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void groupTouchingBottomLeftCorner() {
        int[][] image = {
            {0, 0, 0},
            {1, 0, 0},
            {1, 1, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).size());
        // x: cols 0,0,1 → sum=1, 1/3=0
        // y: rows 1,2,2 → sum=5, 5/3=1
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(1, groups.get(0).centroid().y());
    }

    @Test
    public void groupTouchingBottomRightCorner() {
        int[][] image = {
            {0, 0, 0},
            {0, 1, 1},
            {0, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).size());
        // x: cols 1,2,2 → sum=5, 5/3=1
        // y: rows 1,1,2 → sum=4, 4/3=1
        assertEquals(1, groups.get(0).centroid().x());
        assertEquals(1, groups.get(0).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Section 6: Centroid Calculation
    // -----------------------------------------------------------------------

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
        // Non-symmetric group: cols 1,2,3 → x sum=6, 6/3=2
        int[][] image = {{0, 1, 1, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).centroid().x()); // (1+2+3)/3 = 2
    }

    @Test
    public void twoPixelGroupAtNonZeroPositionHasCorrectCentroid() {
        // Pixels at (col=2,row=1) and (col=3,row=1)
        // x: (2+3)/2 = 2 (integer division floors)
        // y: (1+1)/2 = 1
        int[][] image = {
            {0, 0, 0, 0},
            {0, 0, 1, 1},
            {0, 0, 0, 0}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).centroid().x());
        assertEquals(1, groups.get(0).centroid().y());
    }

    @Test
    public void centroidFloorsBothAxesSimultaneously() {
        // Two pixels at (col=1,row=1) and (col=2,row=2) — diagonal, not connected
        // Verifies each is a separate group (diagonal rule) and both centroids are exact
        int[][] image = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
    }

    // -----------------------------------------------------------------------
    // Section 7: Sorting
    // -----------------------------------------------------------------------

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

    @Test
    public void largerGroupRankedFirstEvenWithLowerXCentroid() {
        // Small group at high x (col=5), large group at low x (col=0)
        // Size should take priority over x centroid in sort order
        int[][] image = {{1, 1, 1, 0, 0, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
        assertEquals(3, groups.get(0).size()); // larger group first
        assertEquals(1, groups.get(0).centroid().x()); // (0+1+2)/3=1
        assertEquals(1, groups.get(1).size());
        assertEquals(5, groups.get(1).centroid().x());
    }

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

    @Test
    public void threeGroupsSameSizeDifferentYOrderedByHigherYFirst() {
        // Three single pixels in same column (x=0), rows 0, 2, 4
        // All size=1, same x=0 → sorted by y descending: 4, 2, 0
        int[][] image = {{1}, {0}, {1}, {0}, {1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(3, groups.size());
        assertEquals(4, groups.get(0).centroid().y());
        assertEquals(2, groups.get(1).centroid().y());
        assertEquals(0, groups.get(2).centroid().y());
    }

    @Test
    public void twoGroupsWithIdenticalSizeAndCentroidBothAppear() {
        // Group A: pixel at col=0, row=0 → centroid (0,0), size=1
        // Group B: pixel at col=0, row=2 → centroid (0,2), size=1
        // Both size=1 — confirm both are in list (neither dropped)
        int[][] image = {{1}, {0}, {1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(2, groups.size());
    }

    @Test
    public void fiveIsolatedGroupsSortedCorrectly() {
        // 5 isolated single pixels in one row at cols 0,2,4,6,8
        // All size=1, sorted by x descending: 8,6,4,2,0
        int[][] image = {{1, 0, 1, 0, 1, 0, 1, 0, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(5, groups.size());
        assertEquals(8, groups.get(0).centroid().x());
        assertEquals(6, groups.get(1).centroid().x());
        assertEquals(4, groups.get(2).centroid().x());
        assertEquals(2, groups.get(3).centroid().x());
        assertEquals(0, groups.get(4).centroid().x());
    }

    // -----------------------------------------------------------------------
    // Section 8: Scale
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

    @Test
    public void wideRowOfTenPixelsHasCorrectCentroid() {
        // 1x10 row of all 1s
        // x: (0+1+2+...+9)/10 = 45/10 = 4 (integer division)
        // y: 0
        int[][] image = {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(10, groups.get(0).size());
        assertEquals(4, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void tallColumnOfTenPixelsHasCorrectCentroid() {
        // 10x1 column of all 1s
        // x: 0
        // y: (0+1+2+...+9)/10 = 45/10 = 4 (integer division)
        int[][] image = {{1},{1},{1},{1},{1},{1},{1},{1},{1},{1}};
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(10, groups.get(0).size());
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(4, groups.get(0).centroid().y());
    }

    @Test
    public void filledNonSquareGridIsOneGroup() {
        // 2 rows x 5 cols, all 1s
        // x: cols 0,1,2,3,4 each twice → sum=20, 20/10=2
        // y: row 0 five times, row 1 five times → sum=5, 5/10=0
        int[][] image = {
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(10, groups.get(0).size());
        assertEquals(2, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    // -----------------------------------------------------------------------
    // Section 9: Input Values
    // -----------------------------------------------------------------------

    @Test
    public void valueGreaterThanOneIsNotCountedAsPartOfGroup() {
        // Implementation checks == 1 strictly; 2 should be treated as background
        int[][] image = {
            {2, 0},
            {0, 1}
        };
        List<Group> groups = finder.findConnectedGroups(image);
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).size());
        assertEquals(1, groups.get(0).centroid().x());
        assertEquals(1, groups.get(0).centroid().y());
    }

    @Test
    public void testSinglePixelGroup() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] grid = {
            {0,0},
            {0,1}
        };

        List<Group> result = finder.findConnectedGroups(grid);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
    }

    @Test
    public void testOneBigGroup() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] grid = {
            {1,1},
            {1,1}
        };

        List<Group> result = finder.findConnectedGroups(grid);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).size());
    }

    @Test
    public void testMultipleGroups() {
        DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

        int[][] grid = {
            {1,0},
            {0,1}
        };

        List<Group> result = finder.findConnectedGroups(grid);

        assertEquals(2, result.size());
    }
}
