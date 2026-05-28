package io.github.konradkelly.centroidfinder;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class VideoController {
    private final VideoCatalogService videoCatalogService;
    private final ThumbnailService thumbnailService;
    private final JobService jobService;
    private final JobStatusMapper jobStatusMapper;

    public VideoController(
        VideoCatalogService videoCatalogService,
        ThumbnailService thumbnailService,
        JobService jobService,
        JobStatusMapper jobStatusMapper
    ) {
        this.videoCatalogService = videoCatalogService;
        this.thumbnailService = thumbnailService;
        this.jobService = jobService;
        this.jobStatusMapper = jobStatusMapper;
    }

    @GetMapping("/api/videos")
    public List<String> listVideos() {
        return videoCatalogService.listVideos();
    }

    @GetMapping(value = "/thumbnail/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getThumbnail(@PathVariable String filename) {
        return thumbnailService.generateThumbnail(filename);
    }

    @PostMapping("/process/{filename}")
    public ResponseEntity<StartJobResponseDto> startProcess(
        @PathVariable String filename,
        @RequestParam(required = false) String targetColor,
        @RequestParam(required = false) String threshold
    ) {
        UUID jobId = jobService.start(filename, targetColor, threshold);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new StartJobResponseDto(jobId.toString()));
    }

    @GetMapping("/process/{jobId}/status")
    public Map<String, String> getStatus(@PathVariable String jobId) {
        UUID id;
        try {
            id = UUID.fromString(jobId);
        } catch (IllegalArgumentException exception) {
            throw new NotFoundException("Job ID not found");
        }

        JobStatusResponseDto response = jobStatusMapper.toResponse(jobService.get(id));
        if ("done".equals(response.status())) {
            return Map.of("status", response.status(), "result", response.result());
        }

        if ("error".equals(response.status())) {
            return Map.of("status", response.status(), "error", response.error());
        }

        return Map.of("status", response.status());
    }
}