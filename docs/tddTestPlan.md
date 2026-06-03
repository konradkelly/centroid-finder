# TDD Test Plan

Follow Red-Green-Refactor for each item: write the failing test first, confirm it fails, then implement.

---

## Improvement 1 — Error Handling: `ApiExceptionHandler.handleFallback` logging

**Branch:** `error-handling/fallback-logging`

**New test class:** `ApiExceptionHandlerTest`

```java
// Arrange: construct ApiExceptionHandler directly (no Spring context needed)
// Act: call handleFallback(new RuntimeException("boom"))
// Assert: returned ErrorResponseDto has a non-null, non-blank message
```

| Test name | Red condition | After fix |
|---|---|---|
| `handleFallbackReturnsGenericErrorResponse` | Passes already (tests return value only) | Still passes |
| `handleFallbackLogsAtErrorLevel` | Fails — no logger exists yet | Passes after adding `log.error(...)` |

**Note:** To assert logging, use [Logback's `ListAppender`](https://logback.qos.ch/manual/appenders.html#ListAppender):
```java
Logger logger = (Logger) LoggerFactory.getLogger(ApiExceptionHandler.class);
ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
listAppender.start();
logger.addAppender(listAppender);
// ... call handleFallback ...
assertThat(listAppender.list).anyMatch(e -> e.getLevel() == Level.ERROR);
```

---

## Improvement 2 — Error Handling: `ThumbnailService` overly broad catch

**Branch:** `error-handling/thumbnail-service-catch`

**Existing test class:** `ThumbnailServiceTest` — add new tests

```java
// Arrange: mock VideoFrameReaderFactory (once factory refactor lands) or use a
//          real ThumbnailService pointed at a corrupt file
// Act: call generateThumbnail("corrupt.mp4")
// Assert: ServerException is thrown AND original cause is preserved (getCause() != null)
```

| Test name | Red condition | After fix |
|---|---|---|
| `generateThumbnailWrapsIllegalStateExceptionWithCause` | Fails — current catch discards cause | Passes after `new ServerException(msg, e)` |
| `generateThumbnailWrapsIllegalArgumentExceptionWithCause` | Fails — same reason | Passes |

---

## Improvement 3 — Performance: `DfsBinaryGroupFinder.adjacentPixels()` inlining

**Branch:** `performance/dfs-adjacency-inlining`

**Existing test class:** `DfsBinaryGroupFinderTest` — run existing suite as regression baseline, add one new timing assertion if desired.

The existing tests are the guard. Before touching `adjacentPixels()`, confirm all pass:

```
mvn test -Dtest=DfsBinaryGroupFinderTest
```

New test to add (documents expected behavior, not performance):

| Test name | Purpose |
|---|---|
| `adjacentPixelsReturnsAllFourNeighboursForCenter` | Verifies the inlined logic still finds all 4 neighbours — guards against off-by-one after removal of the helper |
| `adjacentPixelsOmitsOutOfBoundsNeighbours` | Corner pixel should return only 2 neighbours, not 4 |

These tests target `adjacentPixels()` indirectly through `findConnectedGroups()` with hand-crafted arrays where you know which pixels should be adjacent.

---

## Improvement 4 — Performance: `DistanceImageBinarizer` bulk `getRGB`

**Branch:** `performance/bulk-get-rgb`

**Existing test class:** `DistanceImageBinarizerTest` — existing tests are the regression baseline.

Run before touching anything:
```
mvn test -Dtest=DistanceImageBinarizerTest
```

Add one new test that specifically exercises a larger image to ensure bulk reads produce the same result as per-pixel reads:

| Test name | Purpose |
|---|---|
| `toBinaryArrayProducesCorrectResultForLargeImage` | Creates a 100×100 `BufferedImage` with known pixel values and asserts the binary result matches the expected pattern — guards against bulk-read off-by-one (row/column order swap) |

---

## Improvement 5 — Refactoring: eliminate `JobStore`

**Branch:** `refactoring/eliminate-job-store`

**Existing test class:** `JobServiceTest` — must be updated as part of the refactor.

Steps:
1. **Before changing any production code**, update `JobServiceTest` to inject `JobRepository` directly instead of `JobStore`. The tests will fail to compile — that is the Red state.
2. Update `JobService` to accept `JobRepository` and remove `JobStore` — tests go Green.
3. Delete `JobStore.java`.

| Test name | Change required |
|---|---|
| `startRejectsOutOfRangeThreshold` | Replace `JobStore jobStore = mock(JobStore.class)` with `JobRepository jobRepository = mock(JobRepository.class)` and update `when(jobRepository.save(any()))...` |
| `startCreatesProcessingJobAndSchedulesWorker` | Same mock swap |
| Any other `JobServiceTest` tests | Same mock swap |

---

## Improvement 6 — Documentation: OpenAPI annotations on `VideoController`

**Branch:** `docs/openapi-video-controller`

No functional behavior changes, so no Red-Green cycle is required for this improvement. However, add a smoke test:

**New test (or add to `VideoControllerTest`):**

| Test name | Purpose |
|---|---|
| `swaggerUiEndpointIsReachable` | `GET /swagger-ui/index.html` returns `200 OK` — confirms Springdoc wired up correctly |

```java
mockMvc.perform(get("/swagger-ui/index.html"))
       .andExpect(status().isOk());
```

Note: this test requires `@SpringBootTest` + `@AutoConfigureMockMvc`, not just `@WebMvcTest`, so add it to a separate integration test class or an existing `@SpringBootTest` test.

---

## Run order summary

```
# 1. Verify full suite is green before any branch work
mvn test

# 2. On each branch, run only the relevant test class to stay fast
mvn test -Dtest=ApiExceptionHandlerTest
mvn test -Dtest=ThumbnailServiceTest
mvn test -Dtest=DfsBinaryGroupFinderTest
mvn test -Dtest=DistanceImageBinarizerTest
mvn test -Dtest=JobServiceTest
```
