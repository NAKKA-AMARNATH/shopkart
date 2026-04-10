package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.CreateCategoryRequest;
import com.amarnath.shopkart.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse getCategoryBySlug(String slug);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getTopLevelCategories();

    List<CategoryResponse> getSubcategories(UUID parentId);

    CategoryResponse updateCategory(UUID id, CreateCategoryRequest request);

    void deleteCategory(UUID id);
}