# Decoupling ThumbnailService from JCodecVideoFrameReader

## The Problem

`ThumbnailService` currently instantiates `JCodecVideoFrameReader` directly:

```java
try (VideoFrameReader reader = new JCodecVideoFrameReader(videoPath.toString())) {
```

Even though `VideoFrameReader` is an interface, the service is still tightly coupled to the JCodec implementation. This means:
- Swapping to ffmpeg requires editing `ThumbnailService` directly
- The reader cannot be mocked in unit tests

## The Fix: A Factory Interface

`VideoFrameReader` needs a filename to open — it's not a stateless singleton you can simply inject. The right pattern is a **factory interface** that Spring can inject and that tests can mock.

### 1. Define the factory interface

```java
public interface VideoFrameReaderFactory {
    VideoFrameReader open(String path) throws IOException;
}
```

### 2. Implement it for JCodec

```java
@Component
public class JCodecVideoFrameReaderFactory implements VideoFrameReaderFactory {
    @Override
    public VideoFrameReader open(String path) throws IOException {
        return new JCodecVideoFrameReader(path);
    }
}
```

### 3. Inject the factory into ThumbnailService

```java
@Service
public class ThumbnailService {
    private final ServerPathsProperties paths;
    private final VideoFrameReaderFactory readerFactory;

    public ThumbnailService(ServerPathsProperties paths, VideoFrameReaderFactory readerFactory) {
        this.paths = paths;
        this.readerFactory = readerFactory;
    }

    public byte[] generateThumbnail(String filename) {
        Path videoPath = resolveVideoPath(filename);
        try (VideoFrameReader reader = readerFactory.open(videoPath.toString())) {
            // same logic as before...
        }
    }
}
```

## Swapping to ffmpeg Later

When you implement ffmpeg support, create a new factory implementation and mark it as the active `@Component`:

```java
@Component
public class FfmpegVideoFrameReaderFactory implements VideoFrameReaderFactory {
    @Override
    public VideoFrameReader open(String path) throws IOException {
        return new FfmpegVideoFrameReader(path);
    }
}
```

`ThumbnailService` does not need to change at all — only the factory implementation is swapped.

## Testability Benefit

With the factory injected, `ThumbnailServiceTest` can pass a mock factory without touching the filesystem or a real video file:

```java
VideoFrameReaderFactory mockFactory = mock(VideoFrameReaderFactory.class);
VideoFrameReader mockReader = mock(VideoFrameReader.class);
when(mockFactory.open(any())).thenReturn(mockReader);
when(mockReader.nextFrame()).thenReturn(someFakeFrame);
```
