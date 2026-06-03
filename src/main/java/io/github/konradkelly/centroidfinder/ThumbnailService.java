package io.github.konradkelly.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ThumbnailService {
    private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);
    private final ServerPathsProperties paths;

    public ThumbnailService(ServerPathsProperties paths) {
        this.paths = paths;
    }

    public byte[] generateThumbnail(String filename) {
        Path videoPath = resolveVideoPath(filename);

        try (VideoFrameReader reader = new JCodecVideoFrameReader(videoPath.toString())) {
            FrameSample sample = reader.nextFrame();
            if (sample == null) {
                throw new ServerException("Error generating thumbnail");
            }

            BufferedImage frame = sample.frameImage();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(frame, "jpg", output)) {
                throw new ServerException("Error generating thumbnail");
            }
            return output.toByteArray();
        } catch (IOException | IllegalStateException | IllegalArgumentException exception) {
            log.error("Exception during thumbnail generation", exception);
            throw new ServerException("Error generating thumbnail", exception);
        }
    }

    public Path resolveVideoPath(String filename) {
        validateFilename(filename);
        Path videoPath = paths.videosDir().resolve(filename).normalize();
        if (!videoPath.startsWith(paths.videosDir().normalize())) {
            throw new ValidationException("Invalid filename");
        }

        if (!Files.exists(videoPath) || !Files.isRegularFile(videoPath)) {
            throw new NotFoundException("Video not found");
        }
        return videoPath;
    }

    private void validateFilename(String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ValidationException("Invalid filename");
        }
    }
}