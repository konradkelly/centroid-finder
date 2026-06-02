package io.github.konradkelly.centroidfinder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(VideoControllerTest.TestConfig.class)
public class VideoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoCatalogService videoCatalogService;

    @MockBean
    private ThumbnailService thumbnailService;

    @MockBean
    private JobService jobService;

    @MockBean
    private JobStatusMapper jobStatusMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        ServerPathsProperties serverPathsProperties() {
            return new ServerPathsProperties(
                Path.of("videos"),
                Path.of("results"),
                Path.of("processor.jar"),
                java.time.Duration.ofMinutes(10)
            );
        }
    }

    @Test
    public void listVideosReturnsJsonArray() throws Exception {
        when(videoCatalogService.listVideos()).thenReturn(List.of("a.mp4", "b.mov"));
        mockMvc.perform(get("/api/videos"))
            .andExpect(status().isOk())
            .andExpect(content().json("[\"a.mp4\",\"b.mov\"]"));
    }

    @Test
    public void thumbnailReturnsJpegBytes() throws Exception {
        byte[] bytes = new byte[] {1, 2, 3};
        when(thumbnailService.generateThumbnail("a.mp4")).thenReturn(bytes);
        mockMvc.perform(get("/thumbnail/a.mp4"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(bytes));
    }

    @Test
    public void startProcessReturnsAcceptedWithJobId() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.start("a.mp4", "FFAA11", "20")).thenReturn(jobId);
        mockMvc.perform(post("/process/a.mp4").param("targetColor", "FFAA11").param("threshold", "20"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").value(jobId.toString()));
    }

    @Test
    public void startProcessReturnsBadRequestForMissingQueryParams() throws Exception {
        when(jobService.start(eq("a.mp4"), any(), any()))
            .thenThrow(new ValidationException("Missing targetColor or threshold query parameter."));
        mockMvc.perform(post("/process/a.mp4"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Missing targetColor or threshold query parameter."));
    }

    @Test
    public void getStatusReturnsProcessing() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobEntity job = JobEntity.processing(jobId);
        when(jobService.get(jobId)).thenReturn(job);
        when(jobStatusMapper.toResponse(job)).thenReturn(new JobStatusResponseDto("processing", null, null));
        mockMvc.perform(get("/process/" + jobId + "/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    public void thumbnailReturnsNotFoundWhenVideoMissing() throws Exception {
        when(thumbnailService.generateThumbnail("missing.mp4"))
            .thenThrow(new NotFoundException("Video not found"));

        mockMvc.perform(get("/thumbnail/missing.mp4"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Video not found"));
    }

    @Test
    public void getStatusReturnsNotFoundForUnknownJobId() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(jobService.get(jobId)).thenThrow(new NotFoundException("Job ID not found"));

        mockMvc.perform(get("/process/" + jobId + "/status"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Job ID not found"));
    }

    @Test
    public void getStatusReturnsNotFoundForMalformedJobUuid() throws Exception {
        mockMvc.perform(get("/process/not-a-uuid/status"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Job ID not found"));
    }
   
}
