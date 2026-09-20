package ru.practicum.stats.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StatsRequestDto(
        LocalDateTime start,
        LocalDateTime end,
        List<String> uris,
        Boolean unique
) {
}
