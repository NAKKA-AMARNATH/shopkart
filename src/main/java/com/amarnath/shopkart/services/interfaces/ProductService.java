package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.CreateProductRequest;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.ProductResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request, UUID sellerId);

    ProductResponse getProductById(UUID id);

    ProductResponse getProductBySlug(String slug);

    PagedResponse<ProductResponse> getAllProducts(int page, int size);

    PagedResponse<ProductResponse> getProductsByCategory(UUID categoryId, int page, int size);

    PagedResponse<ProductResponse> searchProducts(
            UUID categoryId,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            int page,
            int size
    );

    ProductResponse updateProduct(UUID id, CreateProductRequest request, UUID sellerId);

    void deleteProduct(UUID id, UUID sellerId);
}