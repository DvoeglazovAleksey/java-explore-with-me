package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.service.CategoryService;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository catRepo;
    private final EventRepository eventRepo;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<Category> categories = catRepo.findAll(pageable);
        return categories.map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(long catId) {
        Category category = catRepo.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto add(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        checkNameIsUnique(newCategoryDto.getName());
        category = catRepo.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto patch(long catId, NewCategoryDto categoryDto) {
        Category category = getCategoryById(catId);
        checkNameIsUnique(categoryDto.getName());
        category.setName(category.getName());
        return categoryMapper.toCategoryDto(catRepo.save(category));
    }

    @Override
    public void delete(long catId) {
        Category category = getCategoryById(catId);
        if (eventRepo.findByCategoryId(catId).isPresent()) {
            throw new ConflictException("Category is connected with events and could be deleted.");
        }
        catRepo.delete(category);
    }

    private Category getCategoryById(Long catId) {
        return catRepo.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private void checkNameIsUnique(String name) {
            catRepo.findFirst1ByName(name).ifPresent(cat -> {
                throw new ConflictException("Category name already exists.");
            });
    }
}