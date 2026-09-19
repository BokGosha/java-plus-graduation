package ru.practicum.main.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.model.Category;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);

    Category updateFromDto(CategoryDto categoryDto);
}