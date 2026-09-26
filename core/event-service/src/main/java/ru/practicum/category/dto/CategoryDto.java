package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDto(
        Long id,
        @NotBlank @Size(min = 1, max = 50) String name
) {
}
