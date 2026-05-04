import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EuclideanColorDistanceTest {

    private static final double EPSILON = 1e-9;

    private final EuclideanColorDistance distanceFinder = new EuclideanColorDistance();

    @Test
    public void sameColorHasZeroDistance() {
        assertEquals(0.0, distanceFinder.distance(0x123456, 0x123456), EPSILON);
    }

    @Test
    public void blackToWhiteIsMaximumDistance() {
        double expected = Math.sqrt((255 * 255) + (255 * 255) + (255 * 255));
        assertEquals(expected, distanceFinder.distance(0x000000, 0xFFFFFF), EPSILON);
    }

    @Test
    public void redChannelOnlyDifference() {
        assertEquals(10.0, distanceFinder.distance(0x0A0000, 0x000000), EPSILON);
    }

    @Test
    public void greenChannelOnlyDifference() {
        assertEquals(25.0, distanceFinder.distance(0x001900, 0x000000), EPSILON);
    }

    @Test
    public void blueChannelOnlyDifference() {
        assertEquals(7.0, distanceFinder.distance(0x000007, 0x000000), EPSILON);
    }

    @Test
    public void mixedChannelDifferenceMatchesPythagoreanCalculation() {
        // Components differ by (3, 4, 12), so distance should be 13.
        assertEquals(13.0, distanceFinder.distance(0x112233, 0x0E1E27), EPSILON);
    }

    @Test
    public void symmetryHoldsForAnyTwoColors() {
        int a = 0xABCDEF;
        int b = 0x123456;

        assertEquals(distanceFinder.distance(a, b), distanceFinder.distance(b, a), EPSILON);
    }

    @Test
    public void nonNegativityAlwaysHolds() {
        assertTrue(distanceFinder.distance(0x000000, 0xFFFFFF) >= 0.0);
        assertTrue(distanceFinder.distance(0x00AA11, 0x00AA11) >= 0.0);
        assertTrue(distanceFinder.distance(0x13579B, 0x2468AC) >= 0.0);
    }

    @Test
    public void distanceIsBoundedByZeroAndMaximumPossibleDistance() {
        double maxDistance = Math.sqrt(3 * 255.0 * 255.0);

        double d1 = distanceFinder.distance(0x010203, 0x040506);
        double d2 = distanceFinder.distance(0x00FF7F, 0x7F00FF);
        double d3 = distanceFinder.distance(0x000000, 0xFFFFFF);

        assertTrue(d1 >= 0.0 && d1 <= maxDistance);
        assertTrue(d2 >= 0.0 && d2 <= maxDistance);
        assertTrue(d3 >= 0.0 && d3 <= maxDistance);
    }

    @Test
    public void alphaBitsAreIgnoredBecauseOnlyRgbIsUsed() {
        // Inputs include alpha bits in the top byte; RGB bytes are identical.
        assertEquals(0.0, distanceFinder.distance(0xFF112233, 0x00112233), EPSILON);
    }

    @Test
    public void worksWithNegativeIntInputsFromArgbValues() {
        // 0xFF000000 as int is negative, but RGB components are all zero.
        assertEquals(0.0, distanceFinder.distance(0xFF000000, 0x00000000), EPSILON);
    }

    @Test
    public void triangleInequalityHoldsForRepresentativeColors() {
        int a = 0x000000;
        int b = 0x112233;
        int c = 0x334455;

        double ab = distanceFinder.distance(a, b);
        double bc = distanceFinder.distance(b, c);
        double ac = distanceFinder.distance(a, c);

        assertTrue(ac <= ab + bc + EPSILON);
    }

    @Test
    public void tinyAdjacentColorDifferenceIsComputedPrecisely() {
        assertEquals(1.0, distanceFinder.distance(0x000000, 0x000001), EPSILON);
    }

    // -----------------------------------------------------------------------
    // Primary and Secondary Color Pair Comparisons
    // -----------------------------------------------------------------------

    @Test
    public void redVsGreen() {
        // (255,0,0) vs (0,255,0): delta R=255, delta G=255, delta B=0
        double expected = Math.sqrt(255 * 255 + 255 * 255);
        assertEquals(expected, distanceFinder.distance(0xFF0000, 0x00FF00), EPSILON);
    }

    @Test
    public void redVsBlue() {
        // (255,0,0) vs (0,0,255): delta R=255, delta G=0, delta B=255
        double expected = Math.sqrt(255 * 255 + 255 * 255);
        assertEquals(expected, distanceFinder.distance(0xFF0000, 0x0000FF), EPSILON);
    }

    @Test
    public void greenVsBlue() {
        // (0,255,0) vs (0,0,255): delta R=0, delta G=255, delta B=255
        double expected = Math.sqrt(255 * 255 + 255 * 255);
        assertEquals(expected, distanceFinder.distance(0x00FF00, 0x0000FF), EPSILON);
    }

    @Test
    public void redVsYellow() {
        // (255,0,0) vs (255,255,0): only green differs by 255
        assertEquals(255.0, distanceFinder.distance(0xFF0000, 0xFFFF00), EPSILON);
    }

    @Test
    public void redVsMagenta() {
        // (255,0,0) vs (255,0,255): only blue differs by 255
        assertEquals(255.0, distanceFinder.distance(0xFF0000, 0xFF00FF), EPSILON);
    }

    @Test
    public void greenVsCyan() {
        // (0,255,0) vs (0,255,255): only blue differs by 255
        assertEquals(255.0, distanceFinder.distance(0x00FF00, 0x00FFFF), EPSILON);
    }

    @Test
    public void blueVsCyan() {
        // (0,0,255) vs (0,255,255): only green differs by 255
        assertEquals(255.0, distanceFinder.distance(0x0000FF, 0x00FFFF), EPSILON);
    }

    @Test
    public void complementaryRedVsCyan() {
        // (255,0,0) vs (0,255,255): all channels differ by 255
        double expected = Math.sqrt(3 * 255 * 255);
        assertEquals(expected, distanceFinder.distance(0xFF0000, 0x00FFFF), EPSILON);
    }

    @Test
    public void complementaryGreenVsMagenta() {
        // (0,255,0) vs (255,0,255): all channels differ by 255
        double expected = Math.sqrt(3 * 255 * 255);
        assertEquals(expected, distanceFinder.distance(0x00FF00, 0xFF00FF), EPSILON);
    }

    @Test
    public void complementaryBlueVsYellow() {
        // (0,0,255) vs (255,255,0): all channels differ by 255
        double expected = Math.sqrt(3 * 255 * 255);
        assertEquals(expected, distanceFinder.distance(0x0000FF, 0xFFFF00), EPSILON);
    }

    @Test
    public void greenVsBlack() {
        // (0,255,0) vs (0,0,0): only green channel = 255
        assertEquals(255.0, distanceFinder.distance(0x00FF00, 0x000000), EPSILON);
    }

    @Test
    public void blueVsBlack() {
        // (0,0,255) vs (0,0,0): only blue channel = 255
        assertEquals(255.0, distanceFinder.distance(0x0000FF, 0x000000), EPSILON);
    }    

    // -----------------------------------------------------------------------
    // Additional Edge Cases
    // -----------------------------------------------------------------------

    @Test
    public void singleRedChannelAtMaximumVsBlack() {
        // Pure red vs black: only red differs by 255
        assertEquals(255.0, distanceFinder.distance(0xFF0000, 0x000000), EPSILON);
    }

    @Test
    public void orthogonalChannelsRedVsBlue() {
        // Pure red vs pure blue: delta R=255, delta G=0, delta B=255
        double expected = Math.sqrt(255 * 255 + 255 * 255);
        assertEquals(expected, distanceFinder.distance(0xFF0000, 0x0000FF), EPSILON);
    }

    @Test
    public void oneChannelDiffersWhenOtherTwoAreNonZeroAndEqual() {
        // R and G identical (0xFF), only B differs by 0x55 = 85
        assertEquals(85.0, distanceFinder.distance(0xFFFF00, 0xFFFF55), EPSILON);
    }

    @Test
    public void sameColorAtMaximumWhiteHasZeroDistance() {
        assertEquals(0.0, distanceFinder.distance(0xFFFFFF, 0xFFFFFF), EPSILON);
    }

    @Test
    public void tinyAdjacentDifferenceAtTopOfRange() {
        // White vs one step below white in blue: only blue differs by 1
        assertEquals(1.0, distanceFinder.distance(0xFFFFFF, 0xFFFFFE), EPSILON);
    }

    @Test
    public void allChannelsDifferByTheSameAmount() {
        // delta R = delta G = delta B = 5
        double expected = Math.sqrt(5 * 5 + 5 * 5 + 5 * 5);
        assertEquals(expected, distanceFinder.distance(0x050505, 0x000000), EPSILON);
    }

    @Test
    public void grayToGrayDistanceWithEqualChannelDeltas() {
        // (128,128,128) vs (64,64,64): all channels differ by 64
        // expected = sqrt(64^2 + 64^2 + 64^2)
        double expected = Math.sqrt(64 * 64 + 64 * 64 + 64 * 64);
        assertEquals(expected, distanceFinder.distance(0x808080, 0x404040), EPSILON);
    }

    @Test
    public void arbitraryRealWorldColorsMatchExternallyVerifiedDistance() {
        // 0xAB4523 = (171, 69, 35), 0x1F7CB2 = (31, 124, 178)
        // delta R=140, delta G=55, delta B=143
        // Verified: sqrt(140^2 + 55^2 + 143^2) ≈ 207.543
        double expected = Math.sqrt(140 * 140 + 55 * 55 + 143 * 143);
        assertEquals(expected, distanceFinder.distance(0xAB4523, 0x1F7CB2), EPSILON);
    }

    @Test
    public void symmetryHoldsForPrimaryColorPair() {
        assertEquals(distanceFinder.distance(0xFF0000, 0x00FF00),
                     distanceFinder.distance(0x00FF00, 0xFF0000), EPSILON);
    }
}