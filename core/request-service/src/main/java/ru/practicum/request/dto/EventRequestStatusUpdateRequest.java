package ru.practicum.request.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record EventRequestStatusUpdateRequest(
        @NotNull List<Long> requestIds,
        @NotNull RequestStatusAction status
) {
}
