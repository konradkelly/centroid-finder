# Project Goals

Theme: Build a scalable video centroid processing backend with better performance and deployment support.

## Easy
- Confirm the centroid pipeline works for static images and sample video frames.
- Add or improve MP4 frame reading and verify timestamped centroid CSV output.
- Use a faster video processing path such as FFmpeg or a better frame extraction library than the current JCodec implementation.

## Goal
- Implement a job queue to process multiple video jobs concurrently.
- Build a simple backend API or command flow that accepts video jobs and returns centroid output.
- Improve performance by using an optimized video reader and reducing redundant processing.

## Stretch
- Deploy the backend to AWS with a CI/CD pipeline.
- Add Redis caching for completed job results and use it to speed repeated requests.
- Support robust job tracking, retries, and queue management for video processing workloads.

## Impossible
- Create a full Android app with Kotlin and Jetpack Compose that records or uploads video and displays centroid overlays live.
- Build a complete real-time interactive dashboard with live video playback, overlayed centroids, and user controls before the quarter ends.
