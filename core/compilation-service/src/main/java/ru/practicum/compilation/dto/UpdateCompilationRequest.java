package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateCompilationRequest(
        List<Long> events,
        Boolean pinned,
        @Size(min = 1, max = 50) String title
) {
}
