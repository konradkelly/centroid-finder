FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/centroid-finder-1.0-SNAPSHOT-boot.jar /app/server.jar
COPY target/centroid-finder-1.0-SNAPSHOT-jar-with-dependencies.jar /app/processor.jar

RUN mkdir -p /app/videos /app/results

ENV VIDEOS_DIR=/app/videos \
    RESULTS_DIR=/app/results \
    VIDEO_PROCESSOR_JAR=/app/processor.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
