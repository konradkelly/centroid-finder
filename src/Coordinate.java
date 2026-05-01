/**
 * Represents a location in an image or array
 * 
 * The top-left cell of the array/image (row:0, column:0) is considered to be coordinate (x:0, y:0).
 * Y increases downward and X increases to the right.
 * 
 * For example, (row:4, column:7) corresponds to (x:7, y:4).
 */
public record Coordinate(int x, int y) {}

 /* ADDING NOTES / STEPS'
this is where the location of the image / array 
top left is x:0, y:0 , y increases downward as x does to the right

 */
