package ru.practicum.request.client.dto;

public record EventInternalDto(
        Long id,
        Long initiatorId,
        EventState state,
        Integer participantLimit,
        Boolean requestModeration
) {
}
