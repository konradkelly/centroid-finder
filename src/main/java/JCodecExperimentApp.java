import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;

/**
 * Small playground app for checking JCodec metadata and frame extraction.
 *
 * Usage:
 *   mvn exec:java -Dexec.mainClass=JCodecExperimentApp -Dexec.args="sampleInput/sample.mp4 5"
 *   mvn exec:java -Dexec.mainClass=JCodecExperimentApp -Dexec.args="sampleInput/sample.mp4 5 jcodec-output.csv"
 */
public class JCodecExperimentApp {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: JCodecExperimentApp <input_mp4_path> [max_frames] [output_csv]");
            return;
        }

        String videoPath = args[0];
        int maxFrames = 5;
        String outputCsvPath = "jcodec-metadata.csv";
        if (args.length >= 2) {
            try {
                maxFrames = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid max_frames value. Using default of 5.");
            }
        }
        if (args.length >= 3) {
            outputCsvPath = args[2];
        }

        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            System.err.println("Video not found: " + videoPath);
            return;
        }

        try (SeekableByteChannel channel = NIOUtils.readableChannel(videoFile)) {
            FrameGrab grab = FrameGrab.createFrameGrab(channel);

            int firstWidth = -1;
            int firstHeight = -1;
            String firstColorSpace = "N/A";
            String firstCrop = "N/A";

            Picture frame;
            int frameIndex = 0;
            while (frameIndex < maxFrames && (frame = grab.getNativeFrame()) != null) {
                if (frameIndex == 0) {
                    firstWidth = frame.getWidth();
                    firstHeight = frame.getHeight();
                    firstColorSpace = String.valueOf(frame.getColor());
                    firstCrop = String.valueOf(frame.getCrop());

                    System.out.println("First frame dimensions: " + firstWidth + "x" + firstHeight);
                    System.out.println("Color space: " + firstColorSpace);
                    System.out.println("Crop: " + firstCrop);
                }
                frameIndex++;
            }

            System.out.println("Frames extracted: " + frameIndex);

            String csvHeader = "video_path,first_width,first_height,color_space,crop,frames_extracted,max_frames_requested";
            String csvRow = escapeCsv(videoPath) + ","
                    + firstWidth + ","
                    + firstHeight + ","
                    + escapeCsv(firstColorSpace) + ","
                    + escapeCsv(firstCrop) + ","
                    + frameIndex + ","
                    + maxFrames;

            Path outputPath = Path.of(outputCsvPath);
            Files.writeString(outputPath, csvHeader + System.lineSeparator() + csvRow + System.lineSeparator());
            System.out.println("Metadata written to: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("I/O error reading video file.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to decode video with JCodec.");
            e.printStackTrace();
        }
    }

    private static String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        String escaped = safeValue.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
