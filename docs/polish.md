# Centroid Finder

Centroid Finder is a Java project that analyzes images and videos to locate objects based on a given color. It finds the centroid (the average position of a group of matching pixels) for each detected object, and writes a timestamped location to a CSV. 

This was built as a part of a software development course. This application applies the DFS (Depth-First-Search) algorithm to identify targeted pixels.

## Architecture

**Image Processing** - handles color distance calculations, binarizing images by color threshold, and writing centroid groups to CSV

**Video Processing** - uses JavaCV and FFmpeg to read frames; each frame gets binarized and analyzed, with centroid positions tracked and written out as a timestamped CSV

**Server** - Spring Boot REST API for listing videos, generating thumbnails, submitting jobs, and polling status

## Challenges

The biggest challenge was finding a threshold value that consistently identified the object. The same applied to selecting a target color. One video we used was a recording of a salamander. Many times there would be gaps in its body, or pixels that were not part of the animal would be included in the detection. This showed us how sensitive image processing algorithms can be to various lighting conditions and color variation within a frame.

![Ensantina threshold demo](ensantina-thresholds.gif)

## Requirements

- Java 17 or higher
- Maven
- JavaCV (FFmpeg bindings) for video frame extraction
- JUnit 5
- Docker
- PostgreSQL

## Prerequisites

PostgreSQL must be running with a `centroid_finder` database before starting the server.

```powershell
docker run -d -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=centroid_finder -p 5432:5432 postgres:16-alpine
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

Server runs at `http://localhost:8080`.

## Running the Video Processing CLI

```powershell
# Analyze video and write centroid CSV
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar input.mp4 output.csv FF0000 100

# Extract first frame as a JPEG thumbnail
java -jar target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar thumbnail input.mp4 thumbnail.jpg
```

Arguments: `inputPath outputCsv targetColor(hex) threshold(0-442)`

## API Endpoints

- `GET /api/videos` — list filenames of videos in `VIDEOS_DIR`
- `GET /thumbnail/{filename}` — returns a JPEG of the first frame
- `POST /process/{filename}?targetColor=FF0000&threshold=100` — queues a job and returns a job ID
  ```json
  { "jobId": "550e8401-e29b-41d4-a716-446655430900" }
  ```
- `GET /process/{jobId}/status` — get the current job status and result location
  ```json
  // While running:
  { "status": "processing" }

  // On success:
  { "status": "done", "result": "results/video_FF0000_100.csv" }

  // On failure:
  { "status": "error", "error": "Job timed out after 10m" }
  ```
- `GET /videos/{filename}` — access the video file
- `GET /results/{filename}` — download the result CSV

### Example Output CSV Data

```
timestamp,x,y
0,112,205
1,118,210
2,121,207
3,-1,-1
```

Each row represents one second of the video. The x and y coordinates `-1,-1` is returned when no matching pixels are detected.

## Environment Variables

- `VIDEOS_DIR` (default `./videos`) — path to videos
- `RESULTS_DIR` (default `./results`) — path to CSVs
- `SPRING_DATASOURCE_URL` (default `jdbc:postgresql://localhost:5432/centroid_finder`)
- `SPRING_DATASOURCE_USERNAME` (default `postgres`)
- `SPRING_DATASOURCE_PASSWORD` (default `postgres`)
- `JOB_TIMEOUT` (default `10m`) — sets the max time a job can run

## Contributing

Pull requests are welcome. For bigger changes, open an issue first.

Make sure to update or add JUnit tests before submitting.

Developed by Konrad Kelly and Fredrick Karau 2026