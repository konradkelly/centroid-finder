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