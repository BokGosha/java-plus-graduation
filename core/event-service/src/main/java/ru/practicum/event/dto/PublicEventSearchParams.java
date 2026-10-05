package ru.practicum.event.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;

import java.util.List;

@Builder
public record PublicEventSearchParams(
        String text,
        List<Long> categories,
        Boolean paid,
        String rangeStart,
        String rangeEnd,
        Boolean onlyAvailable,
        String sort,
        int from,
        int size,
        HttpServletRequest request
) {
}
