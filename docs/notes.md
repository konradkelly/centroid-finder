# Centroid Finder Notes

## ImageSummaryApp

The app takes in three arguments:
1. A path to the image file
2. An RGB color
3. A threshold value that determines the degree of binarization.

The application first parses the image from a hexadecimal string to a 24-bit integer. This integer is then used in a Euclidean Distance Calculation that assigns white (1) to pixels below the threshold value and black (0) to those above. After binarization completes, the formed groups of white pixels are computed to find the centroid. Lastly, data is written to a CSV file.

---

## ImageBinarizer

This interface contains two methods: `toBinaryArray()` and `toBufferedImage()`.

- `toBinaryArray()` converts a BufferedImage into a 2D array of 0s and 1s.
- `toBufferedImage()` does the opposite, converting the 2D array back to a BufferedImage. 0s should be shown with `0x000000` (black) and 1s as `0xFFFFFF` (white).

---

## ColorDistanceFinder

An interface that outlines a method to calculate the color distance between two colors. Implementations should use bit shifting and masking. This interface is implemented by `EuclideanColorDistance`.

---

## EuclideanColorDistance

Calculates the distance between two hexadecimal colors. In Euclidean space, colors are computed based on how close they approximate the red, green, and blue channels. The algorithm takes in two 24-bit integers to determine their distance.

---

## BinarizingImageGroupFinder

`BinarizingImageGroupFinder` implements `ImageGroupFinder`. Its constructor takes:
- An `ImageBinarizer` (converts a `BufferedImage` to a 2D binary array of 0s and 1s)
- A `BinaryGroupFinder` (finds connected groups of 1s/white pixels in that array)

`findConnectedGroups()` returns a `List<Group>`, where each `Group` holds the pixel count (size) and the centroid's x and y coordinates, sorted in descending order.

---

## BinaryGroupFinder

Interface for finding connected groups of 1s in a 2D integer array.

- Input: a non-empty rectangular 2D array containing only 1s and 0s.
- Throws `NullPointerException` if the array or any subarray is null.
- Throws `IllegalArgumentException` if the array is otherwise invalid.
- Pixels are connected horizontally and vertically -- NOT diagonally.
- Coordinate system: top-left cell is (x:0, y:0). Y increases downward, X increases to the right. E.g., (row:4, column:7) -> (x:7, y:4).
- Centroid uses INTEGER DIVISION:
  - x centroid = sum of all x coordinates / number of pixels in group
  - y centroid = sum of all y coordinates / number of pixels in group
- Groups sorted in DESCENDING order by `Group.compareTo()`: size first, then descending y, then descending x.

/* FRED'S NOTES 
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

---

## DfsBinaryGroupFinder

`findConnectedGroups` finds groups of 1s in a 2D integer array and returns:
- `size()` -- the number of pixels in the blob
- Centroid (x, y) -- sum of coordinates divided by group size using INTEGER DIVISION

Blobs are connected horizontally and vertically, NOT diagonally.

## Coordinate
 /* FRED'S NOTES 
this is where the location of the image / array 
top left is x:0, y:0 , y increases downward as x does to the right
 */

 ## DistanceImageBinarizer
 /* FRED NOTES
    // This constructor initializes the DistanceImageBinarizer with the values needed for binarization.
    // It takes a ColorDistanceFinder to calculate how different a pixel’s color is from a target color.
    // The targetColor is the reference color, stored as a 24-bit RGB value (0xRRGGBB).
    // The threshold determines the cutoff: pixels closer than the threshold become white (1),
    // while pixels farther away become black (0).

     // The constructor sets up how the image will be converted to black and white.
    // It takes a ColorDistanceFinder to measure how different two colors are.
    // The targetColor is the reference color, stored as a 24-bit RGB value (0xRRGGBB).
    // The threshold acts as a cutoff: pixels close to the target become white (1),
    // while pixels farther away become black (0).

    // The toBinaryArray method loops through every pixel in the image, computes its distance
    // from the target color, and stores either 0 or 1 in a 2D array based on the threshold.
    
    // The toBufferedImage method does the reverse: it takes a 2D array of 0s and 1s and
    // creates an image where 1 becomes white (0xFFFFFF) and 0 becomes black (0x000000).
    // Together, these methods convert between a full-color image and a simplified binary version.

*/

## GROUP
/* FRED NOTES
   This is is the group of the contiguous pixel
   
   the top left (x: 0, y: 0) --> y increases downward as x increases to the right. eg row:4 column:7 corresponds to x:7, y:4

   we are supposed to return a list of sorted groups , the size being the number of pixels in the group, 
   the centroid being the average of each picel location
   eg
   x coordinate of centroid is the sum of all x coordinates / number of pixela in the group --> basically the average
   y coordinate of centroid is the sum of all y coordinates / number of pixela in rhe group --> basically the average

  the groups are supposed to be in descending order using the groups compareTO method, with the largest group being first, smallest --> last, ties are browken
  the desceding y value first then the descending x

  you're supposed to return it as a CSV row representing the group size and coordinates
   
   */
    
## IMAGEGROUP FINDER
/* FRED'S NOTES 
    Here you basically find the connected grops, the groups are sorted in descending order using the compareTO method
    You return the connected gropus in descending order
 */