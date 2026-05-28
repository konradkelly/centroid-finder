ackage io.github.konradkelly.centroidfinder;

import java.util.List;

public interface BinaryGroupFinder {
   /**
    * Finds connected pixel groups of 1s in an integer array representing a binary image.
    *
    * Pixels are considered connected vertically and horizontally, NOT diagonally.
    * The top-left cell of the array (row:0, column:0) is considered to be coordinate
    * (x:0, y:0). Y increases downward and X increases to the right.
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid is computed using INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method
    * (size first, then x, then y).
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
   public List<Group> findConnectedGroups(int[][] image);
}