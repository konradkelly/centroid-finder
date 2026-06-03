package io.github.konradkelly.centroidfinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiExceptionHandlerTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger handlerLogger;

    @BeforeEach
    public void setUp() {
        handlerLogger = (Logger) LoggerFactory.getLogger(ApiExceptionHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        handlerLogger.addAppender(listAppender);
    }

    @AfterEach
    public void tearDown() {
        handlerLogger.detachAppender(listAppender);
    }

    @Test
    public void handleFallbackLogsAtErrorLevel() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        RuntimeException cause = new RuntimeException("unexpected failure");

        handler.handleFallback(cause);

        boolean errorLogged = listAppender.list.stream()
            .anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getThrowableProxy() != null
                && event.getThrowableProxy().getMessage().equals("unexpected failure"));
        assertTrue(errorLogged, "handleFallback should log the exception at ERROR level");
    }

    @Test
    public void handleFallbackReturnsInternalServerError() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<ErrorResponseDto> response = handler.handleFallback(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server error", response.getBody().error());
    }
}
