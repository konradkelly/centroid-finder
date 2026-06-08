# Centroid Finder

Centroid Finder is a Java project that analyzes images and videos to locate objects based on a given color. It finds the centroid (the average position of a group of matching pixels) for each detected object, and records that location over time as a CSV.

## Requirements

- Java 17 or higher
- Maven 3.8+
- JavaCV (FFmpeg bindings) for video frame extraction
- JUnit 5
- Docker
- PostgreSQL

## Prerequisites

PostgreSQL must be running with a `centroid_finder` database before starting the server.

```powershell
docker run -d -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=centroid_finder -p 5432:5432 postgres:16-alpine
```

If port 5432 is already in use (e.g. a local PostgreSQL install), map to a different host port instead:

```powershell
docker run -d -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=centroid_finder -p 5433:5432 postgres:16-alpine
```

Then set the URL override when starting the server:

```powershell
java -Dspring.datasource.url=jdbc:postgresql://localhost:5433/centroid_finder -jar target/centroid-finder-1.0-SNAPSHOT.jar
```

For local dev without PostgreSQL at all, use the in-memory H2 profile:

```powershell
java -Dspring.profiles.active=test -jar target/centroid-finder-1.0-SNAPSHOT.jar
```

## Build & Installation

Build and package with Maven:

```powershell
mvn clean package
```

## Testing

```powershell
# Unit tests
mvn test

# Integration tests
mvn -P integration test
```

## Running the Server

```powershell
java -jar target/centroid-finder-1.0-SNAPSHOT.jar
```

Starts on `http://localhost:8080`.

## Running the CLI (video analysis)

```powershell
# Extract centroids frame-by-frame to CSV
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar input.mp4 output.csv FF0000 25

# Extract first frame as a JPEG thumbnail
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar thumbnail input.mp4 thumb.jpg
```

Arguments: `inputPath outputCsv targetColor(hex) threshold(0-442)`

## API

- `GET /api/videos` — list filenames of videos in `VIDEOS_DIR`
- `GET /thumbnail/{filename}` — first-frame of JPEG thumbnail
- `POST /process/{filename}?targetColor=FF0000&threshold=25` — start a job and then return a job ID
- `GET /process/{jobId}/status` — check job status and result path when done
- `GET /videos/{filename}` — access the video file
- `GET /results/{filename}` — access the result CSV

## Environment Variables

- `VIDEOS_DIR` (default `./videos`) — path to videos
- `RESULTS_DIR` (default `./results`) — path to CSVs
- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/centroid_finder`)
- `SPRING_DATASOURCE_USERNAME` (default `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default `postgres`)
- `JOB_TIMEOUT` (default `10m`) — sets the max time a job can run

## Project Structure

**Image Processing** - color distance and centroid calculations, image binarization, and CSV file generation

**Video Processing** - reads video frames with JavaCV and FFmpeg, analyzing each frame individually, tracking centroids over time, writing timestamped results to CSV files

**Server** - Spring Boot REST API for serving video catalog, thumbnail generation, job submission, job status tracking, and result output

## Contributing

Pull requests are welcome. For bigger changes, open an issue first.

Make sure to update or add JUnit tests before submitting.


Developed by Konrad Kelly and Fredrick Karau
