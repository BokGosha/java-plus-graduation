package ru.practicum.compilation.dto;

import lombok.Builder;
import ru.practicum.compilation.client.dto.EventShortDto;

import java.util.List;

@Builder
public record CompilationDto(
        Long id,
        String title,
        Boolean pinned,
        List<EventShortDto> events
) {
}
