# Code Review: Suggested Improvements

## Bugs

- **`JCodecVideoFrameReader` constructor — duplicated cleanup code**: Both `catch` blocks for `IOException`/`JCodecException` and `IllegalArgumentException` independently repeat the channel-close logic. Consolidate with a `finally` block.
- **`JCodecVideoFrameReader.close()` — not idempotent**: The test `closeCanBeCalledTwiceWithoutThrowing` documents this expectation, but the implementation will throw on the second call. Add a closed-state guard.
- **`DfsBinaryGroupFinder` — jagged arrays not validated**: Null rows are checked, but rows of inconsistent length are not. A non-rectangular array will cause `ArrayIndexOutOfBoundsException` instead of `IllegalArgumentException`.
- **`VideoController.getStatus` — wrong status code on bad UUID**: A malformed UUID triggers `NotFoundException` (404), but a bad format is a client error and should be 400.
- **`CsvResultWriter` — write errors go undetected**: `PrintWriter.println()` silently swallows I/O errors. Check `writer.checkError()` after writing, or switch to a writer that throws on failure.

---

## Security

- **No authentication or authorization**: The entire REST API (`/api/videos`, `/process/`, `/thumbnail/`) is open. The `/videos/**` and `/results/**` static file routes also expose raw files. If this is ever deployed beyond localhost, access control is essential.
- **`application.yml` — hardcoded default credentials**: `password: postgres` as a default is risky. Ensure there's no path where these defaults reach a real environment.
- **`ddl-auto: update` in production config**: This can silently alter or lose schema on startup. Prefer `validate` with a migration tool (Flyway/Liquibase) for any non-throwaway deployment.

---

## Tests

- **Major coverage gaps on the server side**: `VideoCatalogService`, `ThumbnailService`, `VideoCentroidPipeline`, `DefaultJobProcessLauncher`, and `ApiExceptionHandler` have no unit tests at all.
- **`JobServiceTest` — missing `get()` test**: No test verifies that `get()` throws `NotFoundException` for an unknown ID.
- **`VideoControllerTest` — missing error path tests**: No test for thumbnail 404 (video not found), or for `getStatus` returning 404 for an unknown job ID, or for a malformed job UUID.
- **`VideoProcessorApp` — NullPointerException as a documented contract**: The test `mainPropagatesNullPointerExceptionWhenInputPathValueIsNull` records an NPE as expected behavior. A null args entry should probably produce a user-facing error message rather than a stack trace.

---

## Performance

- **`adjacentPixels()` in `DfsBinaryGroupFinder` allocates on every pixel**: Creating a new `ArrayList` and four `int[]` objects per pixel is heavy for large images. Inline the four directional checks with simple conditionals instead.
- **`image.getRGB(x, y)` in a per-pixel loop in `DistanceImageBinarizer`**: This is slow for large frames. `BufferedImage` offers a bulk `getRGB(x, y, w, h, ...)` call or raster-level access that's significantly faster.

---

## Error Handling

- **`ApiExceptionHandler.handleFallback` — no logging**: The catch-all `Exception` handler swallows the stack trace entirely. At minimum, log the exception at `ERROR` level before returning the generic response so you can trace production issues.
- **`ThumbnailService` — overly broad catch**: Catching `IllegalStateException` and `IllegalArgumentException` in `generateThumbnail` can mask unexpected bugs. Log the original exception before wrapping it in `ServerException`.
- **`JobService` — all non-timeout failures become `"Processor failed"`**: Distinguishing between launch errors (e.g., `IOException` launching the subprocess) and process exit failures would make debugging much easier.

---

## Refactoring

- **Leftover student scaffolding comments in `DistanceImageBinarizer` and `BinaryGroupFinder`**: The `/* ADDING NOTES / STEPS */` blocks are informal notes, not documentation. Remove or replace them with proper Javadoc.
- **`salamanderSearch` method name in `DfsBinaryGroupFinder`**: Rename to something descriptive like `collectConnectedPixels` or `dfsCollect`.
- **Raw `int[]` for pixel coordinates in `DfsBinaryGroupFinder`**: The `{row, col}` arrays have no named fields, making the code fragile. A small record (or reuse of `Coordinate`) would improve clarity.
- **`JobStore` is a pointless wrapper**: It only delegates directly to `JobRepository`. Either add real logic (caching, retry) or eliminate it and inject `JobRepository` directly.
- **`VideoController` has no consistent URL prefix**: The class-level `@RequestMapping` is empty, leaving routes scattered (`/api/videos`, `/thumbnail/...`, `/process/...`). Establish a consistent prefix.
- **Decouple `ThumbnailService` from `JCodecVideoFrameReader`**: `ThumbnailService` hard-codes `new JCodecVideoFrameReader(...)` despite `VideoFrameReader` being an interface. Introduce a `VideoFrameReaderFactory` interface, implement it as a `@Component`, and inject it into the service — enabling implementation swaps (e.g., ffmpeg) and proper unit testing without touching the filesystem. See [video-frame-reader-factory.md](video-frame-reader-factory.md) for full details.

---

## Documentation

- **REST API is undocumented**: No Swagger/OpenAPI annotations on `VideoController`. Document each endpoint's path variables, query parameters, response shapes, and possible error codes.
- **`ServerPathsProperties`**: The config record has no Javadoc explaining what each property controls or any valid ranges/constraints (e.g., `jobTimeout` minimum value).
- **`DefaultJobProcessLauncher`**: The Spring Boot `PropertiesLauncher`-based invocation strategy is non-obvious. A comment explaining why this specific launch approach is needed would help future maintainers.
