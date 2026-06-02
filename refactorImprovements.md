# Refactoring Improvements

## Improvement 1: Eliminate `JobStore`

### Problem
`JobStore` was a thin wrapper over `JobRepository` that added no logic of its own:

```java
@Component
public class JobStore {
    private final JobRepository repository;
    public JobEntity createProcessing(UUID id) { return repository.save(JobEntity.processing(id)); }
    public Optional<JobEntity> find(UUID id) { return repository.findById(id); }
    public JobEntity save(JobEntity job) { return repository.save(job); }
}
```

Every method delegated directly to `JobRepository` with no transformation. This extra layer added indirection and maintenance cost for no benefit.

### Fix
Deleted `JobStore.java` and injected `JobRepository` directly into `JobService`. The three call sites were updated:

| Before | After |
|---|---|
| `jobStore.createProcessing(jobId)` | `jobRepository.save(JobEntity.processing(jobId))` |
| `jobStore.find(jobId)` | `jobRepository.findById(jobId)` |
| `jobStore.save(job)` | `jobRepository.save(job)` |

### TDD Process
- **Red**: Updated `JobServiceTest` to mock `JobRepository` directly and pass it to the `JobService` constructor. This caused 5 compile errors because `JobService` still expected a `JobStore`.
- **Green**: Updated `JobService` to accept `JobRepository`, replaced all three call sites, and deleted `JobStore.java`. All 5 tests passed.

---

## Improvement 2: Decouple `ThumbnailService` from `JCodecVideoFrameReader` via Factory

### Problem
`ThumbnailService` hard-coded the `JCodecVideoFrameReader` implementation:

```java
try (VideoFrameReader reader = new JCodecVideoFrameReader(videoPath.toString())) {
```

Although `VideoFrameReader` is an interface, using `new` at the call site locked `ThumbnailService` to JCodec. This meant:
- Swapping to a different video library (e.g., ffmpeg) required editing `ThumbnailService` directly.
- `generateThumbnail()` could not be unit tested — every test needed a real video file on disk.

### Fix
Introduced a `VideoFrameReaderFactory` interface that `ThumbnailService` now accepts via constructor injection:

```java
public interface VideoFrameReaderFactory {
    VideoFrameReader open(String path) throws IOException;
}
```

`JCodecVideoFrameReaderFactory` implements it as a Spring `@Component`:

```java
@Component
public class JCodecVideoFrameReaderFactory implements VideoFrameReaderFactory {
    @Override
    public VideoFrameReader open(String path) throws IOException {
        return new JCodecVideoFrameReader(path);
    }
}
```

`ThumbnailService` now uses the injected factory:

```java
try (VideoFrameReader reader = readerFactory.open(videoPath.toString())) {
```

Swapping to a different implementation in the future only requires providing a new `@Component` — `ThumbnailService` does not need to change.

### TDD Process
- **Red**: Updated `ThumbnailServiceTest` to construct `ThumbnailService` with a mock `VideoFrameReaderFactory`, and added three new `generateThumbnail` tests. This caused compile errors because `VideoFrameReaderFactory` did not exist yet.
- **Green**: Created `VideoFrameReaderFactory`, `JCodecVideoFrameReaderFactory`, and updated `ThumbnailService`. All 5 tests (2 existing + 3 new) passed.

### New Tests Added
| Test | What it verifies |
|---|---|
| `generateThumbnailReturnsBytesFromFirstFrame` | Returns JPEG bytes when the reader yields a valid frame |
| `generateThumbnailThrowsServerExceptionWhenReaderReturnsNull` | Throws `ServerException` when `nextFrame()` returns null |
| `generateThumbnailThrowsServerExceptionWhenFactoryThrowsIOException` | Throws `ServerException` when the factory itself fails to open the file |
