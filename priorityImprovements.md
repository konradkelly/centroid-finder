## Bugs

- **`JCodecVideoFrameReader.close()` — not idempotent**: The test `closeCanBeCalledTwiceWithoutThrowing` documents this expectation, but the implementation will throw on the second call. Add a closed-state guard.

- **`CsvResultWriter` — write errors go undetected**: `PrintWriter.println()` silently swallows I/O errors. Check `writer.checkError()` after writing, or switch to a writer that throws on failure.

---

## Security

- **No authentication or authorization**: The entire REST API (`/api/videos`, `/process/`, `/thumbnail/`) is open. The `/videos/**` and `/results/**` static file routes also expose raw files. If this is ever deployed beyond localhost, access control is essential.
- **`application.yml` — hardcoded default credentials**: `password: postgres` as a default is risky. Ensure there's no path where these defaults reach a real environment.

---

## Tests

- **Major coverage gaps on the server side**: `VideoCatalogService`, `ThumbnailService`, `VideoCentroidPipeline`, `DefaultJobProcessLauncher`, and `ApiExceptionHandler` have no unit tests at all.
- **`VideoControllerTest` — missing error path tests**: No test for thumbnail 404 (video not found), or for `getStatus` returning 404 for an unknown job ID, or for a malformed job UUID.

---

## Performance

- **`adjacentPixels()` in `DfsBinaryGroupFinder` allocates on every pixel**: Creating a new `ArrayList` and four `int[]` objects per pixel is heavy for large images. Inline the four directional checks with simple conditionals instead.
- **`image.getRGB(x, y)` in a per-pixel loop in `DistanceImageBinarizer`**: This is slow for large frames. `BufferedImage` offers a bulk `getRGB(x, y, w, h, ...)` call or raster-level access that's significantly faster.

