import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class DfsBinaryGroupFinder implements BinaryGroupFinder {
   /**
    * Finds connected pixel groups of 1s in an integer array representing a binary image.
    * 
    * The input is a non-empty rectangular 2D array containing only 1s and 0s.
    * If the array or any of its subarrays are null, a NullPointerException
    * is thrown. If the array is otherwise invalid, an IllegalArgumentException
    * is thrown.
    *
    * Pixels are considered connected vertically and horizontally, NOT diagonally.
    * The top-left cell of the array (row:0, column:0) is considered to be coordinate
    * (x:0, y:0). Y increases downward and X increases to the right. For example,
    * (row:4, column:7) corresponds to (x:7, y:4).
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid of the group
    * is computed as the average of each of the pixel locations across each dimension.
    * For example, the x coordinate of the centroid is the sum of all the x
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * Similarly, the y coordinate of the centroid is the sum of all the y
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * The division should be done as INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        if (image == null) throw new NullPointerException();
            for (int[] row : image) {
            if (row == null) throw new NullPointerException();
    }

        if (image.length == 0 || image[0].length == 0) throw new IllegalArgumentException();
            int[][] visited = new int[image.length][image[0].length];
            List<Group> groups = findConnectedGroupsHelper(image, visited);
            Collections.sort(groups, Collections.reverseOrder());
            return groups;
        }
    
    private static List<Group> findConnectedGroupsHelper(int[][] image, int[][] visited) {
        List<Group> groups = new ArrayList<>();
        for (int row = 0; row < image.length; row++) {
            for (int col = 0; col < image[0].length; col++) {
                if (image[row][col] == 1 && visited[row][col] == 0) {
                    int[] salamander = salamanderSearch(image, new int[]{row, col}, visited);
                    int size = salamander[0];
                    int centroidX = salamander[1] / size;
                    int centroidY = salamander[2] / size;
                    groups.add(new Group(size, new Coordinate(centroidX, centroidY)));
                }
            }
        }
        return groups;
    }

    private static int[] salamanderSearch(int[][] image, int[] current, int[][] visited) {
        int row = current[0];
        int col = current[1];

        if (visited[row][col] == 1 || image[row][col] == 0) return new int[]{0, 0, 0};

        visited[row][col] = 1;

        int[] data = {1, col, row};

        for (int[] pixel : adjacentPixels(image, current)) {
            int[] vals = salamanderSearch(image, pixel, visited);
            data[0] += vals[0];
            data[1] += vals[1];
            data[2] += vals[2];
        }
        return data;
    }

    private static List<int[]> adjacentPixels(int[][] image, int[] current) {
        int curR = current[0];
        int curC = current[1];
        
        List<int[]> adjacentPixels = new ArrayList<>();

        int[][] directions = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        };

        for (int[] dir : directions) {
            int newR = curR + dir[0];
            int newC = curC + dir[1];

            if (newR >= 0 && newR < image.length && newC >= 0 && newC < image[0].length) {
                adjacentPixels.add(new int[]{newR, newC});
            }
        }
        return adjacentPixels;
    }
}
