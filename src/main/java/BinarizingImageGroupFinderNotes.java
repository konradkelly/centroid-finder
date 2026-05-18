/*
BinarizingImageGroupFinder implements ImageGroupFinder. Its constructor takes
an ImageBinarizer (converts a BufferedImage to a 2D binary array of 0s and 1s) and
a BinaryGroupFinder (finds connected groups of 1s/white pixels in that array).

findConnectedGroups() returns a List<Group>, where each Group holds the pixel count 
(size) and the centroid's x and y coordinates, sorted in descending order.
 */