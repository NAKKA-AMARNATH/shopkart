package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.config.CacheConfig;
import com.amarnath.shopkart.dto.request.CreateCategoryRequest;
import com.amarnath.shopkart.dto.response.CategoryResponse;
import com.amarnath.shopkart.entities.Category;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.CategoryRepository;
import com.amarnath.shopkart.services.interfaces.CategoryService;
import com.amarnath.shopkart.utils.SlugUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    // ── CREATE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true),
    })
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String slug = SlugUtils.generateSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Category with this name already exists");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parent(parent)
                .isActive(true)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    // ── READ ───────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "#id")
    public CategoryResponse getCategoryById(UUID id) {
        return mapToResponse(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'slug:' + #slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with slug: " + slug));
        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'all'")
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'top-level'")
    public List<CategoryResponse> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CACHE_CATEGORIES, key = "'subcategories:' + #parentId")
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        findCategoryById(parentId);
        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true),
    })
    public CategoryResponse updateCategory(UUID id, CreateCategoryRequest request) {
        Category category = findCategoryById(id);

        String newSlug = SlugUtils.generateSlug(request.getName());

        if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
            throw new BusinessException("Category with this name already exists");
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category.setName(request.getName());
        category.setSlug(newSlug);
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        return mapToResponse(categoryRepository.save(category));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true),
    })
    public void deleteCategory(UUID id) {
        Category category = findCategoryById(id);

        if (!category.getChildren().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete category that has subcategories. Delete subcategories first.");
        }

        categoryRepository.delete(category);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────
    private Category findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
    }

    private CategoryResponse mapToResponse(Category category) {
        List<CategoryResponse> childResponses = category.getChildren()
                .stream()
                .map(child -> CategoryResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .slug(child.getSlug())
                        .description(child.getDescription())
                        .imageUrl(child.getImageUrl())
                        .isActive(child.isActive())
                        .parentId(category.getId())
                        .parentName(category.getName())
                        .children(List.of())
                        .build())
                .toList();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .isActive(category.isActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(childResponses)
                .build();
    }
}