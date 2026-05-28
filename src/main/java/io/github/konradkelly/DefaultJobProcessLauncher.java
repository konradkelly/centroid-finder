package io.github.konradkelly.centroidfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DefaultJobProcessLauncher implements JobProcessLauncher {
    private static final int MAX_DIAGNOSTICS_CHARS = 8 * 1024;

    @Override
    public JobProcessResult launch(
        Path processorJar,
        Path inputVideo,
        Path outputPath,
        String targetColor,
        int threshold,
        Duration timeout
    ) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "java",
            "-Dloader.main=io.github.konradkelly.centroidfinder.VideoProcessorApp",
            "-cp",
            processorJar.toString(),
            "org.springframework.boot.loader.launch.PropertiesLauncher",
            "analyze",
            inputVideo.toString(),
            outputPath.toString(),
            targetColor,
            Integer.toString(threshold)
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder diagnostics = new StringBuilder();
        Thread reader = new Thread(() -> drainOutput(process, diagnostics), "job-process-output-reader");
        reader.setDaemon(true);
        reader.start();

        boolean exited = timeout == null || timeout.isZero() || timeout.isNegative()
            ? waitForever(process)
            : process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);

        if (!exited) {
            process.destroy();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
            reader.join(1000);
            return JobProcessResult.timedOut(diagnostics.toString());
        }

        reader.join(1000);
        return JobProcessResult.completed(process.exitValue(), diagnostics.toString());
    }

    private boolean waitForever(Process process) throws InterruptedException {
        process.waitFor();
        return true;
    }

    private void drainOutput(Process process, StringBuilder sink) {
        try (BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int read;
            while ((read = bufferedReader.read(buffer)) != -1) {
                synchronized (sink) {
                    int remaining = MAX_DIAGNOSTICS_CHARS - sink.length();
                    if (remaining <= 0) {
                        continue;
                    }
                    sink.append(buffer, 0, Math.min(read, remaining));
                }
            }
        } catch (IOException ignored) {
            // process closed; output capture is best-effort
        }
    }
}