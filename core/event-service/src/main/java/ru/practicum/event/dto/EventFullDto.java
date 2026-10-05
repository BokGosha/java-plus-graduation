package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.event.client.dto.UserShortDto;

import java.time.LocalDateTime;

public record EventFullDto(
        Long id,
        String annotation,
        CategoryDto category,
        Long confirmedRequests,
        Long comments,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdOn,
        String description,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime eventDate,
        UserShortDto initiator,
        LocationDto location,
        Boolean paid,
        Integer participantLimit,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime publishedOn,
        Boolean requestModeration,
        String state,
        String title,
        Long views
) {
}
