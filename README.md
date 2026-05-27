# centroid-finder

## *DO THIS FIRST* Wave 0: AI Rules 
AI is *NOT ALLOWED* for generating implementations of the classes.
AI is allowed for helping you make test cases.

Don't have it just create the tests mindlessly for you though! Make sure you're actively involved in making the tests.

DO NOT MIX HUMAN AND AI COMMITS.
EVERY COMMIT THAT USES AI MUST START WITH THE COMMIT MESSAGE "AI Used" AND IT MUST ONLY CREATE/ALTER TEST FILES

For this wave, please have each partner make a commit below with their username acknowledging that they understand the rules, according to the following format:

"I, KARAU_1218, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

"I, konradkelly, understand that AI is ONLY to be used for tests, and that every commit that I use AI for must start with 'AI Used'"

## Wave 1: Understand
Read through ImageSummaryApp in detail with your partner. Understand what each part does. This will involve looking through and reading ALL of the other classes records and interfaces. This will take a long time, but it is worth it! Do not skimp on this part, you will regret it! Also look at the sampleInput and sampleOutput folders to understand what comes in and what goes out.

As you read through the files, take notes in notes.md to help you and your partner understand. Make frequent commits to your notes.

## Wave 2: Implement DfsBinaryGroupFinder
This class takes in a binary image array and finds the connected groups. It will look very similar in many ways to the explorer problem you did for DFS! You'll need to understand the Group record to do this well.

Consider STARTING with the unit tests. Remember, you can use AI to help with the unit tests but NOT the implementation. Any AI commit must start with the message "AI Used"

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.

VS Code testing extension not working? Use this command:
```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path src --scan-class-path
```

## Wave 3: Implement EuclideanColorDistance
Implement EuclideanColorDistance. You may consider adding a helper method for converting a hex int into R, G, and B components.

Again, consider starting with unit tests. You may consider using WolframAlpha to help you get correct expected values.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS.
VS Code testing extension not working? Use this command:
```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path src --scan-class-path
```

## Wave 4: Implement DistanceImageBinarizer
To do this you will need to research `java.awt.image.BufferedImage`. In particular, make sure to understand `getRGB` and `setRGB`. When creating a new image, you can use the below to start the instance:

```
new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
```

Note that a lot of this class will be calling methods in BinaryGroupFinder and ColorDistanceFinder!

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here.

VS Code testing extension not working? Use this command:
```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path src --scan-class-path
```

HINT: `getRGB` returns a 32-bit AARRGGBB color (includes alpha channel). However, ColorDistanceFinder expects the colors to come in RRGGBB format (no alpha channel (most significant 8 bits set to 0)). What can you do to make this conversion happen?

## Wave 5: Implement BinarizingImageGroupFinder
This implementation will be relatively short! It will mostly be calling methods in ImageBinarizer and BinaryGroupFinder.

MAKE SURE YOU MAKE THOROUGH UNIT TESTS. Consider asking the AI to teach you about mocks and fakes in unit testing and how they may be helpful here. I recommend NOT using any external library other than JUnit. If the AI wants to use another external library, consider asking it not to and to make stubs instead.

VS Code testing extension not working? Use this command:
```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -jar lib/junit-platform-console-standalone-1.12.0.jar --class-path src --scan-class-path
```

## Wave 6: Validation
To validate your code is working, make sure you're in the centroid-finder directory and run the below command:

```
javac -cp lib/junit-platform-console-standalone-1.12.0.jar src/*.java && java -cp src ImageSummaryApp sampleInput/squares.jpg FFA200 164
```

This will compile your files and run the main method in ImageSummaryApp against the sample image with a target color of orange and a threshold of 164. It should binarized.png and groups.csv which should match the corresponding files in the sampleOutput directory.

Once you have confirmed it is working, clean up your code, make sure it's committed and pushed, and make a PR to submit. Great job!

## Wave 7: Video Processing Restructuring
In this wave, we prepare the project for MP4 support by restructuring centroid-finder into a Maven project and selecting a Java video processing library. No video centroid tracking is implemented yet; this step is only setup so the next assignment can process videos and output timestamped centroid CSV data.

---

## Branch Overview

The project has grown across three branches, each building on the previous:

| Branch | Purpose |
|---|---|
| `main` | Waves 1–6 complete: image centroid finder for static images, flat `src/main/java` layout, compiled and run with `javac`/`java`. |
| `video` | Wave 7+: Maven restructure + JCodec MP4 reader; full frame-by-frame centroid pipeline producing a timestamped CSV. |
| `server` | Spring Boot REST API that wraps the `video` branch CLI as a subprocess, adds job tracking and a PostgreSQL-backed job store. |

---

## Main Branch

`main` contains the completed Waves 1–6 implementation. Source files live directly under `src/main/java` (no Maven, no packages).

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

