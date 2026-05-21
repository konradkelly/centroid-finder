## New Classes To Create

1. VideoProcessorApp
- Main entry point for CLI execution.
- Parses args and starts the pipeline.

2. VideoProcessingConfig
- Stores validated CLI values:
- inputPath
- outputCsvPath
- targetColor
- threshold

3. CliArgumentParser
- Converts raw string arguments into VideoProcessingConfig.
- Handles args count/formatting errors.

4. VideoFrameReader (interface)
- Defines how frames are retrieved with timestamps.

5. JCodecVideoFrameReader (implementation)
- Uses JCodec to read mp4 frames.
- Produces frames with time metadata.

6. FrameSample
- Data object that contains:
- timestampSeconds
- frameImage (or frame data type used by your project)

7. FrameCentroidAnalyzer
- For each frame:
- binarize using target color + threshold
- finds groups of salamander pixels
- select the largest group
- return centroid coordinate or missing result

8. TimestampedCentroidResult
- Data object holding:
- timestampSeconds
- x
- y

9. CsvResultWriter
- Writes rows to output CSV:
- timestamp, x, y

10. VideoCentroidPipeline
- Coordinates the full workflow:
- read frame sample
- analyze frame
- write CSV row
- continue until all frames are read