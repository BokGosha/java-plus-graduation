package ru.practicum.exception;

import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.error("404 {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("The required object was not found.")
                .status("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();
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
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("409 {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("Integrity constraint has been violated.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.error("409 {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("For the requested operation the conditions are not met.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({
            ServiceUnavailableException.class,
            NoFallbackAvailableException.class,
            CallNotPermittedException.class,
            RetryableException.class,
            FeignException.class
    })
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiError handleServiceUnavailableException(final Exception e) {
        log.error("503 {}", e.getMessage());
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("Required service is unavailable.")
                .status("SERVICE_UNAVAILABLE")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(final Exception e) {
        log.error("500 Internal Server Error: ", e);
        return ApiError.builder()
                .errors(List.of(e.getClass().getSimpleName()))
                .message(e.getMessage())
                .reason("Internal server error.")
                .status("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
