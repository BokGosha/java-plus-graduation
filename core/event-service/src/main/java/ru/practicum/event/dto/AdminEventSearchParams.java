package ru.practicum.event.dto;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AdminEventSearchParams(
        List<Long> users,
        List<String> states,
        List<Long> categories,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
        Integer from,
        Integer size
) {
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10;

    public AdminEventSearchParams {
        if (from == null || from < 0) {
            from = DEFAULT_FROM;
        }
        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }
    }
}
