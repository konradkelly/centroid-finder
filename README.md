# centroid-finder

A Java application that finds the centroid of the largest connected group of pixels matching a target color — in static images and MP4 videos. A Spring Boot REST API wraps the video processor for async job dispatch and result retrieval.

---

## Branch Overview

The project has grown across three branches, each building on the previous:

| Branch | Purpose |
|---|---|
| `main` | Image centroid finder for static images; flat `src/main/java` layout, compiled and run with `javac`/`java`. |
| `video` | Maven restructure + JCodec MP4 reader; full frame-by-frame centroid pipeline producing a timestamped CSV. |
| `server` | Spring Boot REST API that wraps the `video` branch CLI as a subprocess, adds job tracking and a PostgreSQL-backed job store. |

---

## Main Branch

`main` contains the static image centroid finder. Source files live directly under `src/main/java` (no Maven, no packages).

### Key classes

| Class | Role |
|---|---|
| `ImageSummaryApp` | Entry point: accepts an image path, target color, and threshold; writes `binarized.png` and `groups.csv`. |
| `DfsBinaryGroupFinder` | Finds connected pixel groups in a binary image using iterative DFS. |
| `EuclideanColorDistance` | Computes Euclidean RGB distance between two colors (ignoring alpha). |
| `DistanceImageBinarizer` | Converts a `BufferedImage` to a binary array by comparing each pixel to the target color. |
| `BinarizingImageGroupFinder` | Combines binarizer and group finder into a single `ImageGroupFinder` implementation. |

### Build and Run

```bash
# Compile all sources
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java

# Run against a sample image
java -cp src ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

Outputs `binarized.png` and `groups.csv` in the working directory. Compare against `sampleOutput/` to validate.

### Run tests

```bash
java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path src --scan-class-path
```

---

## Video Branch: MP4 Centroid Tracking

The `video` branch restructures the project as a Maven build and adds a complete frame-by-frame centroid extraction pipeline for MP4 files.

### Key classes

| Class | Role |
|---|---|
| `VideoProcessorApp` | CLI entry point; dispatches to `analyze` or `thumbnail` mode. |
| `CliArgumentParser` | Parses and validates CLI arguments into a `VideoProcessingConfig` record. |
| `VideoProcessingConfig` | Immutable record: `inputPath`, `outputCsvPath`, `targetColor` (int), `threshold` (int). |
| `VideoFrameReader` | Interface for frame-by-frame iteration with auto-close (`Closeable`). |
| `JCodecVideoFrameReader` | JCodec-backed implementation; detects frame rate and converts each `Picture` to `BufferedImage`. |
| `FrameSample` | Data record: `timestampSeconds` (double) + `frameImage` (`BufferedImage`). |
| `FrameCentroidAnalyzer` | Binarizes each frame, finds connected groups, picks the largest, returns its centroid. |
| `TimestampedCentroidResult` | Data record: `timestampSeconds`, `x`, `y` (sentinel `-1` when no group is found). |
| `CsvResultWriter` | Writes `timestamp,x,y` rows; formats whole-second timestamps without a decimal point. |
| `VideoCentroidPipeline` | Coordinates the full loop: read → analyze → write, until EOF. |

### Build

```powershell
# Produces two jars: plain jar and shaded fat jar (JCodec bundled)
mvn clean package
```

### Run

```powershell
# Analyze: extract centroids frame-by-frame
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar inputVideo.mp4 output.csv FFAA11 20

# Thumbnail: extract the first frame as a JPEG
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar thumbnail inputVideo.mp4 thumb.jpg
```

Arguments for `analyze` mode (positional):

| Argument | Description |
|---|---|
| `inputPath` | Path to the `.mp4` file. |
| `outputCsv` | Path for the output CSV file (created or overwritten). |
| `targetColor` | 6-digit hex RGB, e.g. `FFAA11`. |
| `threshold` | Euclidean distance threshold in `[0, 442]`. |

### Output CSV format

```
timestampSeconds,x,y
0,312,210
0.5,315,208
```

Rows are written for every frame. When no matching color group exists, `x` and `y` are `-1`.

### Iterative DFS

`DfsBinaryGroupFinder` was refactored from recursive to iterative (explicit stack) to prevent `StackOverflowError` on large connected components at high thresholds. Centroid and group-size behavior is unchanged.

### Run tests

```powershell
mvn test
```

---

## Server: Running, Configuration, and API

The Spring Boot server exposes a small REST API around the existing video-processing CLI. It serves video metadata, generates thumbnails on demand, dispatches the processor as a subprocess, and tracks job state in a database.

### Build and Run

```powershell
mvn clean package
java -jar target/centroid-finder-1.0-SNAPSHOT.jar
```

Defaults bind to `http://localhost:8080`. The packaged processor jar
(`target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar`) is invoked per
job; ensure it is built (`mvn package` produces both jars).

### Persistence

Job state is stored using **Spring Data JPA** backed by **Hibernate** as the ORM. In production, Hibernate maps the `Job` entity to a PostgreSQL table and generates SQL automatically. In tests, the datasource is swapped for an **H2 in-memory database** — no external process required. See [docs/hibernate-and-h2.md](docs/hibernate-and-h2.md) for details.

### Environment Variables

All settings have sensible defaults; override via environment variables or `application.yml`.

| Variable | Default | Description |
|---|---|---|
| `VIDEOS_DIR` | `./videos` | Directory scanned by `GET /api/videos` and used as input for `POST /process/{filename}`. |
| `RESULTS_DIR` | `./results` | Output directory for generated centroid CSV files. Served under `/results/**`. |
| `VIDEO_PROCESSOR_JAR` | `./target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar` | Path to the shaded CLI jar that the server launches as a subprocess. |
| `JOB_TIMEOUT` | `10m` | Max wall-clock duration for a single processor subprocess. Accepts ISO-8601 duration syntax (`30s`, `5m`, `1h`). Jobs that exceed this are killed and marked `error` with message `Processor timed out`. |
| `SPRING_DATASOURCE_URL` | — | JDBC URL for the job repository (PostgreSQL in prod). |
| `SPRING_DATASOURCE_USERNAME` | — | DB username. |
| `SPRING_DATASOURCE_PASSWORD` | — | DB password. |

### Endpoints

All JSON responses use UTF-8. Error responses use a single envelope: `{ "error": "<message>" }`.

#### `GET /api/videos`
Lists video filenames available under `VIDEOS_DIR`.

- **200**: `["clip-a.mp4", "clip-b.mp4"]`

#### `GET /thumbnail/{filename}`
Returns a JPEG thumbnail of the first frame of the named video.

- **200**: `image/jpeg` bytes.
- **404**: `{ "error": "..." }` if the file is missing or filename is invalid.

#### `POST /process/{filename}?targetColor={hex}&threshold={int}`
Starts an asynchronous centroid-extraction job.

- **Query params**:
  - `targetColor` — 6-digit hex RGB (e.g. `FFAA11`).
  - `threshold` — integer in `[0, 442]` (Euclidean RGB distance bound).
- **202 Accepted**: `{ "jobId": "<uuid>" }`
- **400 Bad Request**: `{ "error": "..." }` for invalid filename, color, or threshold.

#### `GET /process/{jobId}/status`
Polls job state. Status is one of `processing`, `done`, or `error`.

- **200 (processing)**: `{ "status": "processing" }`
- **200 (done)**: `{ "status": "done", "result": "/results/<filename>.csv" }`
- **200 (error)**: `{ "status": "error", "error": "<safe client message>" }`
- **404**: `{ "error": "Job ID not found" }` for unknown or malformed UUIDs.

Client-visible error messages from a failed job are intentionally generic:
- `Processor timed out`
- `Processor did not produce an output file`
- `Processor failed`

Detailed stdout/stderr from the subprocess (capped at 8KB) is logged at `WARN` level on the server alongside the job ID.

#### Static Routes
- `GET /videos/{filename}` — direct file access into `VIDEOS_DIR`.
- `GET /results/{filename}` — direct file access into `RESULTS_DIR`.

### Sample Session

```powershell
# 1. List available videos
curl http://localhost:8080/api/videos

# 2. Get a thumbnail
curl -o thumb.jpg http://localhost:8080/thumbnail/ensantina.mp4

# 3. Start a job
$resp = curl -X POST "http://localhost:8080/process/ensantina.mp4?targetColor=FFAA11&threshold=20"
$jobId = ($resp | ConvertFrom-Json).jobId

# 4. Poll status
curl "http://localhost:8080/process/$jobId/status"

# 5. Fetch the result CSV
curl "http://localhost:8080/results/ensantina.mp4.csv"
```

### Testing

```powershell
# Unit tests (fast, default)
mvn test

# Integration tests (Spring context + mocked subprocess)
mvn -P integration test
```

### Troubleshooting

- **`Could not autowire ServerPathsProperties`** — Ensure `application.yml` has the `app:` block, or set the env vars above.
- **`Processor did not produce an output file`** — The subprocess exited cleanly but wrote nothing. Check server logs (WARN level) for captured stdout/stderr and confirm `VIDEO_PROCESSOR_JAR` points at the shaded jar with dependencies.
- **`Processor timed out`** — Job exceeded `JOB_TIMEOUT`. Increase the limit (e.g. `JOB_TIMEOUT=30m`) for large videos.
- **PostgreSQL connection refused locally** — The default profile expects a running PostgreSQL on the URL in `SPRING_DATASOURCE_URL`. For local dev without Postgres, run with `-Dspring.profiles.active=test` to use the in-memory H2 datasource configured under `src/test/resources/application.yml`.
- **Integration tests skipped by `mvn test`** — That is intentional: `**/*IntegrationTest.java` is excluded from the default Surefire run. Use `mvn -P integration test` to include them.

