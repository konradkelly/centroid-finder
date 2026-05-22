package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

public class VideoProcessorAppTest {
    private static final String USAGE_MESSAGE =
        "Usage: java -jar videoprocessor.jar inputPath outputCsv targetColor threshold";

    private byte[] runMainAndCaptureErrBytes(String[] args) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        try {
            System.setErr(new PrintStream(errContent, true, StandardCharsets.UTF_8));
            VideoProcessorApp.main(args);
        } finally {
            System.setErr(originalErr);
        }

        return errContent.toByteArray();
    }

    private String runMainAndCaptureErrText(String[] args) {
        return new String(runMainAndCaptureErrBytes(args), StandardCharsets.UTF_8);
    }

    @Test
    public void mainPrintsUsageWhenArgumentsAreMissing() {
        String errorOutput = runMainAndCaptureErrText(new String[] {"only-one-arg"});
        assertTrue(errorOutput.contains("Usage: java -jar videoprocessor.jar"));
    }

    @Test
    public void mainPrintsExpectedUsageBytesWhenArgumentsAreMissing() {
        String expectedMessage = USAGE_MESSAGE + System.lineSeparator();
        byte[] expectedBytes = expectedMessage.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = runMainAndCaptureErrBytes(new String[] {"only-one-arg"});

        assertArrayEquals(expectedBytes, actualBytes);
    }

    @Test
    public void mainPrintsUsageWhenArgumentsAreNull() {
        String errorOutput = runMainAndCaptureErrText(null);

        assertEquals(USAGE_MESSAGE + System.lineSeparator(), errorOutput);
    }

    @Test
    public void mainPrintsTargetColorErrorWhenTargetColorIsInvalid() {
        String errorOutput = runMainAndCaptureErrText(new String[] {"in.mp4", "out.csv", "not-hex", "25"});

        assertEquals(
            "targetColor must be a hex RGB value in RRGGBB format" + System.lineSeparator(),
            errorOutput
        );
    }

    @Test
    public void mainPrintsThresholdErrorWhenThresholdIsInvalid() {
        String errorOutput = runMainAndCaptureErrText(new String[] {"in.mp4", "out.csv", "FF0000", "NaN"});

        assertEquals("threshold must be an integer" + System.lineSeparator(), errorOutput);
    }

    @Test
    public void mainPrintsVideoOpenErrorWhenInputPathDoesNotExist() {
        String errorOutput = runMainAndCaptureErrText(
            new String[] {"definitely-missing-video-file-xyz.mp4", "out.csv", "FF0000", "25"}
        );

        assertEquals(
            "Unable to open video: definitely-missing-video-file-xyz.mp4" + System.lineSeparator(),
            errorOutput
        );
    }

    @Test
    public void mainPrintsOutputOpenErrorWhenOutputPathIsInvalid() {
        String invalidOutputPath = Path.of("this", "path", "does", "not", "exist", "results.csv").toString();

        String errorOutput = runMainAndCaptureErrText(
            new String[] {"sampleInput/pian_niu.mp4", invalidOutputPath, "FF0000", "25"}
        );

        assertEquals("Unable to open output CSV: " + invalidOutputPath + System.lineSeparator(), errorOutput);
    }

    @Test
    public void mainRunsPipelineAndWritesCsvWhenArgumentsAreValid() throws Exception {
        Path outputFile = Files.createTempFile("video-processor-app", ".csv");

        String errorOutput = runMainAndCaptureErrText(
            new String[] {"sampleInput/pian_niu.mp4", outputFile.toString(), "FF0000", "25"}
        );

        assertEquals("", errorOutput);
        assertTrue(Files.exists(outputFile));
        assertFalse(Files.readString(outputFile).isBlank());
    }

    @Test
    public void mainPropagatesNullPointerExceptionWhenInputPathValueIsNull() {
        assertThrows(
            NullPointerException.class,
            () -> VideoProcessorApp.main(new String[] {null, "out.csv", "FF0000", "25"})
        );
    }
}
