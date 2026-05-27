package io.github.konradkelly.centroidfinder;

import org.springframework.stereotype.Component;

@Component
public class JobStatusMapper {
    public JobStatusResponseDto toResponse(JobEntity job) {
        if (job.getStatus() == JobStatus.DONE) {
            return new JobStatusResponseDto("done", job.getResultPath(), null);
        }

        if (job.getStatus() == JobStatus.ERROR) {
            return new JobStatusResponseDto("error", null, job.getErrorMessage());
        }

        return new JobStatusResponseDto("processing", null, null);
    }
}
