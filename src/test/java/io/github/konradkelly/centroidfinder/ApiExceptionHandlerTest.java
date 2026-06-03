package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.ResponseEntity;
import org.junit.jupiter.api.Test;

public class ApiExceptionHandlerTest {
    @Test
    public void handleValidationReturnsBadRequest() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        ValidationException ex = new ValidationException("bad");
        ResponseEntity<ErrorResponseDto> resp = handler.handleValidation(ex);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("bad", resp.getBody().error());
    }

    @Test
    public void handleNotFoundReturns404() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        NotFoundException ex = new NotFoundException("missing");
        ResponseEntity<ErrorResponseDto> resp = handler.handleNotFound(ex);
        assertEquals(404, resp.getStatusCodeValue());
        assertEquals("missing", resp.getBody().error());
    }

    @Test
    public void handleServerReturns500() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        ServerException ex = new ServerException("boom");
        ResponseEntity<ErrorResponseDto> resp = handler.handleServer(ex);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("boom", resp.getBody().error());
    }

    @Test
    public void handleFallbackReturnsGeneric500() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        Exception ex = new Exception("x");
        ResponseEntity<ErrorResponseDto> resp = handler.handleFallback(ex);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("Server error", resp.getBody().error());
    }
}
