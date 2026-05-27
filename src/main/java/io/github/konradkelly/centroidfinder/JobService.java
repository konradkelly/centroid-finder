package io.github.konradkelly.centroidfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JobService {
    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final ServerPathsProperties paths;
    private final ThumbnailService thumbnailService;
    private final JobStore jobStore;
    private final JobProcessLauncher jobProcessLauncher;
    private final Executor taskExecutor;

    public JobService(
        ServerPathsProperties paths,
        ThumbnailService thumbnailService,
        JobStore jobStore,
        JobProcessLauncher jobProcessLauncher,
        @Qualifier("applicationTaskExecutor") Executor taskExecutor
    ) {
        this.paths = paths;
        this.thumbnailService = thumbnailService;
        this.jobStore = jobStore;
        this.jobProcessLauncher = jobProcessLauncher;
        this.taskExecutor = taskExecutor;
    }

    public UUID start(String filename, String targetColor, String thresholdValue) {
        Path inputVideo = thumbnailService.resolveVideoPath(filename);
        String normalizedTargetColor = normalizeTargetColor(targetColor);
        int threshold = parseThreshold(thresholdValue);

        UUID jobId = UUID.randomUUID();
        JobEntity job = jobStore.createProcessing(jobId);
        Path outputPath = buildOutputPath(filename, jobId);

        taskExecutor.execute(() -> runProcess(job, inputVideo, outputPath, normalizedTargetColor, threshold));
        return jobId;
    }

    public JobEntity get(UUID jobId) {
        return jobStore.find(jobId).orElseThrow(() -> new NotFoundException("Job ID not found"));
    }

    private void runProcess(JobEntity job, Path inputVideo, Path outputPath, String targetColor, int threshold) {
        try {
            Files.createDirectories(paths.resultsDir());

            JobProcessResult result = jobProcessLauncher.launch(
                paths.videoProcessorJar(),
                inputVideo,
                outputPath,
                targetColor,
                threshold,
                paths.jobTimeout()
            );

            if (result.timedOut()) {
                log.warn(
                    "Job {} timed out after {}. Captured output:\n{}",
                    job.getId(),
                    paths.jobTimeout(),
                    result.diagnostics()
                );
                job.setStatus(JobStatus.ERROR);
                job.setErrorMessage("Processor timed out");
                job.setResultPath(null);
            } else if (result.exitCode() == 0 && Files.exists(outputPath)) {
                job.setStatus(JobStatus.DONE);
                job.setResultPath("/results/" + outputPath.getFileName());
                job.setErrorMessage(null);
            } else if (result.exitCode() == 0) {
                log.warn(
                    "Job {} processor exited 0 but produced no output. Captured output:\n{}",
                    job.getId(),
                    result.diagnostics()
                );
                job.setStatus(JobStatus.ERROR);
                job.setErrorMessage("Processor did not produce an output file");
                job.setResultPath(null);
            } else {
                log.warn(
                    "Job {} processor exited {}. Captured output:\n{}",
                    job.getId(),
                    result.exitCode(),
                    result.diagnostics()
                );
                job.setStatus(JobStatus.ERROR);
                job.setErrorMessage("Processor failed");
                job.setResultPath(null);
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Job {} failed with exception", job.getId(), exception);
            job.setStatus(JobStatus.ERROR);
            job.setErrorMessage("Processor failed");
            job.setResultPath(null);
        }

        jobStore.save(job);
    }

    private Path buildOutputPath(String filename, UUID jobId) {
        String baseName = filename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = filename.substring(0, dotIndex);
        }
        return paths.resultsDir().resolve(baseName + "-" + jobId + ".csv");
    }

    private String normalizeTargetColor(String targetColor) {
        if (targetColor == null || targetColor.isBlank()) {
            throw new ValidationException("Missing targetColor or threshold query parameter.");
        }

        String normalized = targetColor.startsWith("#") ? targetColor.substring(1) : targetColor;
        if (!normalized.matches("(?i)^[0-9a-f]{6}$")) {
            throw new ValidationException("targetColor must be a hex RGB value in RRGGBB format");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private int parseThreshold(String thresholdValue) {
        if (thresholdValue == null || thresholdValue.isBlank()) {
            throw new ValidationException("Missing targetColor or threshold query parameter.");
        }

        int threshold;
        try {
            threshold = Integer.parseInt(thresholdValue);
        } catch (NumberFormatException exception) {
            throw new ValidationException("threshold must be an integer in the range 0-255");
        }

        if (threshold < 0 || threshold > 255) {
            throw new ValidationException("threshold must be an integer in the range 0-255");
        }
        return threshold;
    }
}
