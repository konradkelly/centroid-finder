package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class JobRepositoryIntegrationTest {
    @Autowired
    private JobRepository jobRepository;

    @Test
    public void savesAndLoadsJobEntityWithStatusFields() {
        UUID jobId = UUID.randomUUID();
        JobEntity job = JobEntity.processing(jobId);
        job.setStatus(JobStatus.DONE);
        job.setResultPath("/results/out.csv");

        jobRepository.save(job);

        JobEntity loaded = jobRepository.findById(jobId).orElseThrow();
        assertEquals(JobStatus.DONE, loaded.getStatus());
        assertEquals("/results/out.csv", loaded.getResultPath());
        assertTrue(loaded.getErrorMessage() == null);
    }
}
