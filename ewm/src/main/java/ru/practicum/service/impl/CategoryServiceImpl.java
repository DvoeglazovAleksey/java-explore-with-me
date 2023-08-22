package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto add(NewCategoryDto category) {
        Category newCategory = categoryMapper.toCategory(category);
        Category saved = categoryRepository.save(newCategory);
        return categoryMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = getCategoryIfExists(catId);
        if (eventRepository.findByCategoryId(catId).isPresent()) {
            throw new ConflictException("Category is connected with events and could be deleted.");
        }
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long catId) {
        Category category = getCategoryIfExists(catId);
        String newName = categoryDto.getName();
        String existingName = category.getName();
        category.setName(StringUtils.defaultIfBlank(newName, existingName));
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<Category> catPage = categoryRepository.findAll(pageable);
        return catPage.map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    @Transactional
    public CategoryDto getById(Long catId) {
        Category category = getCategoryIfExists(catId);
        return categoryMapper.toCategoryDto(category);
    }

    private Category getCategoryIfExists(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }
}