package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record NewCompilationDto(
        List<Long> events,
        Boolean pinned,
        @NotBlank @Size(min = 1, max = 50) String title
) {
    public NewCompilationDto {
        if (pinned == null) {
            pinned = false;
        }
    }
}
