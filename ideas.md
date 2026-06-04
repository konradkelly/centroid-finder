## Implementation Ideas

1. AWS deployment for backend, using EC2, RDS, S3 and a GitHub Actions CI/CD Pipeline

2. Create an Event Queue that process multiple Video Processing Jobs at once.

3. Implement caching servers with Redis to cache Jobs.

4. Implement ffmepeg and change JCodec implementation to a faster video processing library to improve application performance.

5. Create Android verison of this application with Kotlin and Jetpack Compose.