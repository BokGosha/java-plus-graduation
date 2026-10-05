package ru.practicum.event.dto;

import ru.practicum.event.model.EventState;

public record EventInternalDto(
        Long id,
        Long initiatorId,
        EventState state,
        Integer participantLimit,
        Boolean requestModeration
) {
}
