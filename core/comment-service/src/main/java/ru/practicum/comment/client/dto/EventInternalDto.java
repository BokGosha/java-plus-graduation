package ru.practicum.comment.client.dto;

public record EventInternalDto(
        Long id,
        EventState state
) {
}
