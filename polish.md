## Centroid Finder
Centroid Finder is a Java project that analyzes images and videos to locate objects based on color and calculates their locations. 


## Requirements
- Java 17 or higher
- Maven 3.8+
- JCodec
- JUnit 5

## Build & Installation
- Use Maven to compile the project and package it into a runnable JAR.
```
mvn clean package
```
- To run all tests 

```
mvn test
```

## Usage
- The project was developed to be a learning object oriented programing, image processing, testing, and backend develpment. 
- The application starts with image processing and  exapnds to video processing that allows you to track the video frames


## Project Structure
Image Processing

The image-processing portion of the project focuses on:

- Color distance calculations
- Image binarization
- Centroid calculations
- CSV output generation

Video Processing

The video-processing portion of the project focuses on:

- Reading video frames with JCodec
- Analyzing each frame individually
- Tracking centroids over time
- Writing timestamped results to CSV files

Server

The Spring Boot server provides:

- Video catalog access
- Thumbnail generation
- Job submission endpoints
- Job status tracking
- Result file management


## Contributing
- Pull requests are welcome! For major changes, please open an issue first so we can chat about what you'd like to change.

- Make sure you update or add JUnit tests before you submit your code.

- Developed By Konrad Kelly and Fredrick Karau
