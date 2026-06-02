## Bugs

- **`JCodecVideoFrameReader.close()` — not idempotent**: The test `closeCanBeCalledTwiceWithoutThrowing` documents this expectation, but the implementation will throw on the second call. Add a closed-state guard.

- **`CsvResultWriter` — write errors go undetected**: `PrintWriter.println()` silently swallows I/O errors. Check `writer.checkError()` after writing, or switch to a writer that throws on failure.