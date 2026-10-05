package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewCommentDto(
        @NotBlank(message = "Текст комментария не может быть пустым")
        @Size(min = 1, max = 1000, message = "Длина комментария должна быть от 1 до 1000 символов")
        String text
) {
}
