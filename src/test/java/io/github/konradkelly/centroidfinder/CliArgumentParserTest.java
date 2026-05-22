package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class CliArgumentParserTest {
    private static final String USAGE_MESSAGE =
        "Usage: java -jar videoprocessor.jar inputPath outputCsv targetColor threshold";

    private final CliArgumentParser parser = new CliArgumentParser();

    @Test
    public void parseConvertsExpectedArguments() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "25"});

        assertEquals("in.mp4", config.inputPath());
        assertEquals("out.csv", config.outputCsvPath());
        assertEquals(0xFF0000, config.targetColor());
        assertEquals(25, config.threshold());
    }

    @Test
    public void parseRejectsNullArgumentsWithUsageMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> parser.parse(null));

        assertEquals(USAGE_MESSAGE, exception.getMessage());
    }

    @Test
    public void parseRejectsTooFewArgumentsWithUsageMessage() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"too", "few"})
        );

        assertEquals(USAGE_MESSAGE, exception.getMessage());
    }

    @Test
    public void parseRejectsTooManyArgumentsWithUsageMessage() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "25", "extra"})
        );

        assertEquals(USAGE_MESSAGE, exception.getMessage());
    }

    @Test
    public void parseAcceptsLowercaseTargetColor() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "ff0000", "25"});

        assertEquals(0xFF0000, config.targetColor());
    }

    @Test
    public void parseAcceptsMixedCaseTargetColor() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "Ff00Aa", "25"});

        assertEquals(0xFF00AA, config.targetColor());
    }

    @Test
    public void parseRejectsInvalidTargetColorWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "not-hex", "25"})
        );

        assertEquals("targetColor must be a hex RGB value in RRGGBB format", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseRejectsNullTargetColorWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", null, "25"})
        );

        assertEquals("targetColor must be a hex RGB value in RRGGBB format", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseRejectsHexPrefixTargetColorWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "0xFF0000", "25"})
        );

        assertEquals("targetColor must be a hex RGB value in RRGGBB format", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseAcceptsShortTargetColorStringWithCurrentBehavior() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "F", "25"});

        assertEquals(0x00000F, config.targetColor());
    }

    @Test
    public void parseRejectsOverflowTargetColorWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "FFFFFFFF", "25"})
        );

        assertEquals("targetColor must be a hex RGB value in RRGGBB format", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseAcceptsZeroThreshold() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "0"});

        assertEquals(0, config.threshold());
    }

    @Test
    public void parseAcceptsNegativeThreshold() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "-1"});

        assertEquals(-1, config.threshold());
    }

    @Test
    public void parseAcceptsPlusPrefixedThreshold() {
        VideoProcessingConfig config = parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "+7"});

        assertEquals(7, config.threshold());
    }

    @Test
    public void parseRejectsInvalidThresholdWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", "NaN"})
        );

        assertEquals("threshold must be an integer", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseRejectsNullThresholdWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", null})
        );

        assertEquals("threshold must be an integer", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parseRejectsWhitespaceThresholdWithMessageAndCause() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse(new String[]{"in.mp4", "out.csv", "FF0000", " 25"})
        );

        assertEquals("threshold must be an integer", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof NumberFormatException);
    }

    @Test
    public void parsePreservesEmptyPathValues() {
        VideoProcessingConfig config = parser.parse(new String[]{"", "", "FF0000", "25"});

        assertEquals("", config.inputPath());
        assertEquals("", config.outputCsvPath());
    }

    @Test
    public void parsePreservesWhitespaceOnlyPathValues() {
        VideoProcessingConfig config = parser.parse(new String[]{"   ", "\t", "FF0000", "25"});

        assertEquals("   ", config.inputPath());
        assertEquals("\t", config.outputCsvPath());
    }

    @Test
    public void parsePreservesNullPathValues() {
        VideoProcessingConfig config = parser.parse(new String[]{null, null, "FF0000", "25"});

        assertEquals(null, config.inputPath());
        assertEquals(null, config.outputCsvPath());
    }
}
