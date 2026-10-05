package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ParticipationRequestDto(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
        Long event,
        Long id,
        Long requester,
        String status
) {
}
