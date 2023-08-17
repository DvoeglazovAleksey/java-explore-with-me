package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(long catId);

    CategoryDto add(NewCategoryDto newCategoryDto);

    CategoryDto patch(long catId, NewCategoryDto newCategoryDto);

    void delete(long catId);
}
