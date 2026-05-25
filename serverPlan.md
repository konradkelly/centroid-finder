### 1. Architecture Direction and New Classes
Use Spring Boot as the API framework and keep the existing video processor as a separate executable JAR.


High-level components:
- `VideoController`: manages HTTP endpoints
- `VideoCatalogService`: lists files in the videos directory
- `ThumbnailService`: gets first frame (will enhance CLI arguments in JAR executable to include analyze and thumbnail modes.)
- `JobService`: creates a job, starts the background process, and exposes the status
- `JobStore`: persists job status in PostgreSQL using Spring Data JPA (Hibernate) with UUID job IDs.
- `StaticFileConfig`: publicly serves videos/results paths

### 2. API Endpoints

## List all available videos
    API Endpoint: `GET /api/videos`
    Expected Behavior: returns all video files from the videos directory.
    HTTP Status Codes: returns 200 on success, or 500 on error.

## Generate thumbnail
    API Endpoint: `GET /thumbnail/{filename}`
    Expected Behavior: return the first frame of the video as JPEG for a thumbnail.
    HTTP Status Codes:
    — returns 200 on success (includes JPEG binary: Content-Type: image/jpeg)
    — 500 on error.

## Start Processing job
    API Endpoint: `POST /process/{filename}?targetColor=<hex>&threshold=<int>`
    Expected Behavior: Starts an asynchronous processing job and returns a jobId.
    Responses: 
    202—Request accepted and job started successfully.
    400 — Missing required query parameters.
    500 — Server error while starting the job.

## Poll Processing status
    API Endpoint: `GET /process/{jobId}/status`

    Expected Behavior: Checks the status of a processing job.
    Responses: 
    200 — Job is still processing - {"status": "processing"}. 
    200 — Job completed successfully - {"status": "done", "result": "/results/<file>.csv"}.
    200 — Job failed during processing - {"status": "error", "error": "..."}.
    404 — Job ID was not found - {"error": "Job ID not found"}.
    500 — Server error while fetching job status - {"error": "Error fetching job status"}.
    
### 3. Configuration and Environment Variables
    Use `.env` for:
    - `VIDEOS_DIR` (absolute path to source videos)
    - `RESULTS_DIR` (absolute path where CSV outputs are written)
    - `VIDEO_PROCESSOR_JAR` (absolute path to processor JAR)

    - Map env vars into `application.yml` placeholders.

### 4. Non-Blocking Background Processing
    Request must not wait for the JAR to finish. Jobs should be asynchronous allowing for concurrency.


    Spring Boot approach:
    - Use `ProcessBuilder` to launch `java -jar <processor.jar> ...`.
    - Launch asynchronously using `@Async` service method or `TaskExecutor`.
    - Store job state before and after process completion.

## 5. Job Lifecycle
    Use UUID for job IDs.


    Job states:
    - `processing`
    - `done` (include public results path)
    - `error` (include safe error summary)


    Lifecycle steps:
    1. Validate request (`filename`, `targetColor`, `threshold`).
    2. Create job record with `processing`.
    3. Return `202` immediately with `jobId`.
    4. Start background worker process for JAR execution.
    5. On completion, write `done` + result path.
    6. On failure, write `error` + message.

## 6. Validation and Error Handling Plan


    Validation rules:
    - `filename` must exist in `VIDEOS_DIR` and reject path traversal.
    - `targetColor` must be valid hex (6 chars in length; optional leading `#`).
    - `threshold` must be integer in the range of `0-255`.


    Error handling:
    - Standardize all error responses to a uniform JSON schema
    - Never expose internal stack traces or implementation details in client responses; record them server-side for diagnostics.

## Architecture Diagram

![Server Diagram](Server_Diagram.png)
