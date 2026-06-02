package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class JobServiceTest {
    private static ServerPathsProperties testProperties() {
        return new ServerPathsProperties(
            Path.of("videos"),
            Path.of("results"),
            Path.of("processor.jar"),
            Duration.ofMinutes(10)
        );
    }

    @Test
    public void startRejectsOutOfRangeThreshold() throws Exception {
        ServerPathsProperties properties = testProperties();

        ThumbnailService thumbnailService = Mockito.mock(ThumbnailService.class);
        when(thumbnailService.resolveVideoPath("a.mp4")).thenReturn(Path.of("videos", "a.mp4"));

        JobRepository jobRepository = Mockito.mock(JobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> JobEntity.processing(((JobEntity) invocation.getArgument(0)).getId()));

        JobProcessLauncher launcher =
            (processorJar, inputVideo, outputPath, targetColor, threshold, timeout) -> JobProcessResult.completed(0, "");

        Executor executor = runnable -> {};
        JobService service = new JobService(properties, thumbnailService, jobRepository, launcher, executor);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> service.start("a.mp4", "FF0000", "300")
        );

        assertEquals("threshold must be an integer in the range 0-255", exception.getMessage());
    }

    @Test
    public void startCreatesProcessingJobAndSchedulesWorker() throws Exception {
        ServerPathsProperties properties = testProperties();

        ThumbnailService thumbnailService = Mockito.mock(ThumbnailService.class);
        when(thumbnailService.resolveVideoPath("a.mp4")).thenReturn(Path.of("videos", "a.mp4"));

        JobRepository jobRepository = Mockito.mock(JobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> JobEntity.processing(((JobEntity) invocation.getArgument(0)).getId()));

        JobProcessLauncher launcher = Mockito.mock(JobProcessLauncher.class);
        when(launcher.launch(any(), any(), any(), any(), anyInt(), any())).thenReturn(JobProcessResult.completed(0, ""));

        final int[] scheduled = {0};
        Executor executor = runnable -> {
            scheduled[0]++;
            runnable.run();
        };

        JobService service = new JobService(properties, thumbnailService, jobRepository, launcher, executor);

        UUID id = service.start("a.mp4", "FF0000", "20");

        assertNotNull(id);
        assertEquals(1, scheduled[0]);
    }

    @Test
    public void startMarksJobAsErrorWhenProcessorDoesNotWriteOutputFile() throws Exception {
        ServerPathsProperties properties = testProperties();

        ThumbnailService thumbnailService = Mockito.mock(ThumbnailService.class);
        when(thumbnailService.resolveVideoPath("a.mp4")).thenReturn(Path.of("videos", "a.mp4"));

        JobRepository jobRepository = Mockito.mock(JobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> JobEntity.processing(((JobEntity) invocation.getArgument(0)).getId()));

        JobProcessLauncher launcher = Mockito.mock(JobProcessLauncher.class);
        when(launcher.launch(any(), any(), any(), any(), anyInt(), any())).thenReturn(JobProcessResult.completed(0, ""));

        Executor executor = Runnable::run;

        JobService service = new JobService(properties, thumbnailService, jobRepository, launcher, executor);

        UUID id = service.start("a.mp4", "FF0000", "20");

        assertNotNull(id);
        verify(jobRepository).save(org.mockito.ArgumentMatchers.argThat(job ->
            job.getStatus() == JobStatus.ERROR &&
            "Processor did not produce an output file".equals(job.getErrorMessage())
        ));
    }

    @Test
    public void startMarksJobAsErrorWhenProcessorTimesOut() throws Exception {
        ServerPathsProperties properties = testProperties();

        ThumbnailService thumbnailService = Mockito.mock(ThumbnailService.class);
        when(thumbnailService.resolveVideoPath("a.mp4")).thenReturn(Path.of("videos", "a.mp4"));

        JobRepository jobRepository = Mockito.mock(JobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> JobEntity.processing(((JobEntity) invocation.getArgument(0)).getId()));

        JobProcessLauncher launcher = Mockito.mock(JobProcessLauncher.class);
        when(launcher.launch(any(), any(), any(), any(), anyInt(), any()))
            .thenReturn(JobProcessResult.timedOut("partial diagnostics"));

        Executor executor = Runnable::run;

        JobService service = new JobService(properties, thumbnailService, jobRepository, launcher, executor);

        UUID id = service.start("a.mp4", "FF0000", "20");

        assertNotNull(id);
        verify(jobRepository).save(org.mockito.ArgumentMatchers.argThat(job ->
            job.getStatus() == JobStatus.ERROR &&
            "Processor timed out".equals(job.getErrorMessage())
        ));
    }

    @Test
    public void startMarksJobAsErrorWithGenericMessageWhenProcessorExitsNonZero() throws Exception {
        ServerPathsProperties properties = testProperties();

        ThumbnailService thumbnailService = Mockito.mock(ThumbnailService.class);
        when(thumbnailService.resolveVideoPath("a.mp4")).thenReturn(Path.of("videos", "a.mp4"));

        JobRepository jobRepository = Mockito.mock(JobRepository.class);
        when(jobRepository.save(any())).thenAnswer(invocation -> JobEntity.processing(((JobEntity) invocation.getArgument(0)).getId()));

        JobProcessLauncher launcher = Mockito.mock(JobProcessLauncher.class);
        when(launcher.launch(any(), any(), any(), any(), anyInt(), any()))
            .thenReturn(JobProcessResult.completed(2, "stack trace details"));

        Executor executor = Runnable::run;

        JobService service = new JobService(properties, thumbnailService, jobRepository, launcher, executor);

        UUID id = service.start("a.mp4", "FF0000", "20");

        assertNotNull(id);
        verify(jobRepository).save(org.mockito.ArgumentMatchers.argThat(job ->
            job.getStatus() == JobStatus.ERROR &&
            "Processor failed".equals(job.getErrorMessage())
        ));
    }
}
