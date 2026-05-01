import java.util.List;

public interface BinaryGroupFinder {
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
    * The groups are sorted in DESCENDING order according to Group's compareTo method
    * (size first, then x, then y). That is, the largest group will be first, the 
    * smallest group will be last, and ties will be broken first by descending 
    * y value, then descending x value.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
   public List<Group> findConnectedGroups(int[][] image);
   /* ADDING NOTES / STEPS
   If array / subarray are null throw a NullPointerException
   if array is invlaud throw an IllegalArgumentException
   
   the top left (x: 0, y: 0) y increases downward as x increases to the right. eg row:4 column:7 corresponds to x:7, y:4

   we are supposed to return a list of sorted groups , the size being the number of pixels in the group, the centroid being the average of each picel location
   eg
   x coordinate of centroid is the sum of all x coordinates / number of pixela in the group --> basically the average
   y coordinate of centroid is the sum of all y coordinates / number of pixela in rhe group --> basically the average

  the groups are supposed to be in descending order using the groups compareTO method, with the largest group being first, smallest --> last, ties are browken
  the desceding y value first then the descending x
   
   */

}