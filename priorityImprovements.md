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

---

## Error Handling

- **`ApiExceptionHandler.handleFallback` — no logging**: The catch-all `Exception` handler swallows the stack trace entirely. At minimum, log the exception at `ERROR` level before returning the generic response so you can trace production issues.
- **`ThumbnailService` — overly broad catch**: Catching `IllegalStateException` and `IllegalArgumentException` in `generateThumbnail` can mask unexpected bugs. Log the original exception before wrapping it in `ServerException`.

---

## Refactoring

- **Decouple `ThumbnailService` from `JCodecVideoFrameReader`**: `ThumbnailService` hard-codes `new JCodecVideoFrameReader(...)` despite `VideoFrameReader` being an interface. Introduce a `VideoFrameReaderFactory` interface, implement it as a `@Component`, and inject it into the service — enabling implementation swaps (e.g., ffmpeg) and proper unit testing without touching the filesystem. See [video-frame-reader-factory.md](video-frame-reader-factory.md) for full details.
- **`JobStore` is a pointless wrapper**: It only delegates directly to `JobRepository`. Either add real logic (caching, retry) or eliminate it and inject `JobRepository` directly.