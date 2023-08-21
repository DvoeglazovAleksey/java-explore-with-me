package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.service.CategoryService;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.error.ConflictException;
import ru.practicum.repository.EventRepository;

import java.util.List;

import static ru.practicum.utils.ExploreConstantsAndStaticMethods.*;

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
        checkNewCatNameIsUnique(category.getName(), null);
        Category saved = categoryRepository.save(newCategory);
        return categoryMapper.toCategoryDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long catId) {
        Category category = getCategoryIfExists(catId);
        checkNoEventWithCatExists(catId);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long catId) {
        Category category = getCategoryIfExists(catId);
        checkNewCatNameIsUnique(categoryDto.getName(), category.getName());
        updateCategoryByDto(category, categoryDto);
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

    private void checkNewCatNameIsUnique(String newName, String name) {
        if (!newName.equals(name)) {
            categoryRepository.findFirst1ByName(newName).ifPresent(cat -> {
                throw new ConflictException(CATEGORY_NAME_ALREADY_EXISTS_EXCEPTION);
            });
        }
    }

    private void checkNoEventWithCatExists(long catId) {
        if (eventRepository.findByCategoryId(catId).isPresent()) {
            throw new ConflictException(CATEGORY_IS_CONNECTED_WITH_EVENTS);
        }
    }

    private Category getCategoryIfExists(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND_EXCEPTION));
    }

    private void updateCategoryByDto(Category category, CategoryDto categoryDto) {
        String newName = categoryDto.getName();
        String existingName = category.getName();
        category.setName(StringUtils.defaultIfBlank(newName, existingName));
    }
}