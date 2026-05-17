/* ImageSummaryApp.java

The app takes in three arguments:
1) A path to the image file
2) An RGB color
3) A threshold value determines the degree of binarization.

The application first parses the image from a Hexidecimal string to a 24-bit integer. This integer is then used in a Euclidean Distance Calculation that assigns the color white to 1s below the threshold value and black to those above. After binarization completes, the formed groups of white are computed to find the centroid. Lastly, data is written to a CSV file.

*/