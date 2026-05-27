package io.github.konradkelly.centroidfinder;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JobStore {
    private final JobRepository repository;

    public JobStore(JobRepository repository) {
        this.repository = repository;
    }

    public JobEntity createProcessing(UUID id) {
        return repository.save(JobEntity.processing(id));
    }

    public Optional<JobEntity> find(UUID id) {
        return repository.findById(id);
    }

    public JobEntity save(JobEntity job) {
        return repository.save(job);
    }
}
