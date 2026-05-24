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
