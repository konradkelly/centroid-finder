## Bugs

- **`JCodecVideoFrameReader.close()` — not idempotent**: The test `closeCanBeCalledTwiceWithoutThrowing` documents this expectation, but the implementation will throw on the second call. Add a closed-state guard.

- **`CsvResultWriter` — write errors go undetected**: `PrintWriter.println()` silently swallows I/O errors. Check `writer.checkError()` after writing, or switch to a writer that throws on failure.

- **No authentication or authorization**: The entire REST API (`/api/videos`, `/process/`, `/thumbnail/`) is open. The `/videos/**` and `/results/**` static file routes also expose raw files. If this is ever deployed beyond localhost, access control is essential.
- **`application.yml` — hardcoded default credentials**: `password: postgres` as a default is risky. Ensure there's no path where these defaults reach a real environment.