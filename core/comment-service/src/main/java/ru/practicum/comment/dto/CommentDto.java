package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.practicum.comment.client.dto.UserShortDto;

import java.time.LocalDateTime;

@Builder
public record CommentDto(
        Long id,
        String text,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime created,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updated,
        String status,
        Long eventId,
        UserShortDto author
) {
}
