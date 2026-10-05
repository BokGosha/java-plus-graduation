package ru.practicum.exception;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({
            NotFoundException.class,
            NoResourceFoundException.class,
            NoHandlerFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final Exception e) {
        log.warn("404 {}", e.getMessage());
        return notFound(e);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) {
        log.error("400 {}", e.getMessage());
        return apiError(e, "Incorrectly made request.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("409 {}", e.getMessage());
        return apiError(e, "Integrity constraint has been violated.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("409 {}", e.getMessage());
        return conflict(e);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleNoFallbackAvailableException(final NoFallbackAvailableException e) {
        Throwable cause = e.getCause();

        while (cause != null) {
            if (cause instanceof NotFoundException notFound) {
                log.error("404 {}", notFound.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound(notFound));
            }
            if (cause instanceof ConflictException conflict) {
                log.error("409 {}", conflict.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict(conflict));
            }
            cause = cause.getCause();
        }

        log.error("503 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(apiError(e, "Required service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE));
    }

    @ExceptionHandler({
            ServiceUnavailableException.class,
            CallNotPermittedException.class,
            RetryableException.class,
            FeignException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleServiceUnavailableException(final Exception e) {
        log.error("503 {}", e.getMessage());
        return apiError(e, "Required service is unavailable.", HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(final Exception e) {
        log.error("500 Internal Server Error: ", e);
        return apiError(e, "Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiError notFound(final Throwable e) {
        return apiError(e, "The required object was not found.", HttpStatus.NOT_FOUND);
    }

    private ApiError conflict(final Throwable e) {
        return apiError(e, "For the requested operation the conditions are not met.", HttpStatus.CONFLICT);
    }

    private ApiError apiError(final Throwable e, final String reason, final HttpStatus status) {
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
