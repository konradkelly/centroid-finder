# Project Goals

Theme: Build a scalable video centroid processing backend with better performance and deployment support.

## Easy
- Confirm the centroid pipeline works for static images and sample video frames.
- Add or improve MP4 frame reading and verify timestamped centroid CSV output.
- Replace JCodec with JavaCV (FFmpeg bindings) for faster video frame extraction.

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

## AWS Deployment
- Scope: AWS deployment for backend and frontend (EC2, RDS, S3, CloudFront, GitHub Actions)

## Easy
- Run the backend on a single EC2 instance.
- Dockerize the app and deploy with a manual script.
- Store uploads/results in one S3 bucket.
- Use basic GitHub Actions checks (build + test only).

## Goal
- Deploy with an Auto Scaling Group behind an ALB.
- Use managed RDS for persistent job metadata.
- Add a GitHub Actions workflow that deploys to one AWS environment.
- Configure environment variables and secrets through GitHub and AWS.
- Provision core infrastructure with Terraform for a single environment.

## Stretch
- Set up dev/stage/prod environments with separate AWS resources.
- Implement blue/green or rolling deployments with rollback support.
- Maintain reusable IaC modules and policy checks across environments.
- Add centralized monitoring, alerting, and structured logging.
- Enforce IAM least privilege, secret rotation, and security scans in CI/CD.

## Impossible
- Deliver globally distributed, fully compliant enterprise infrastructure.
- Guarantee true zero downtime for all releases and failure modes.
- Achieve all of the above without sufficient budget, operations ownership, and SRE/security capacity.
