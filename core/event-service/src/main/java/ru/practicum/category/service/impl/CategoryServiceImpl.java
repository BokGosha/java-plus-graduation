package ru.practicum.category.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.category.dto.CategoryDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        log.info("Adding new category: {}", newCategoryDto.name());

        Category category = categoryMapper.toEntity(newCategoryDto);
        category = categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        log.info("Deleting category with id: {}", catId);

        categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        boolean hasEvents = eventRepository.existsByCategoryId(catId);
        if (hasEvents) {
            throw new ConflictException("The category is not empty");
        }

        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        log.info("Updating category with id: {}", catId);

        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        category.setName(categoryDto.name());
        category = categoryRepository.save(category);

        return categoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageRequest)
                .stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));

        return categoryMapper.toDto(category);
    }
}
