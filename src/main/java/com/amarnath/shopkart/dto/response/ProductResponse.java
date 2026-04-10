package com.amarnath.shopkart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    UUID id;
    String name;
    String slug;
    String description;
    BigDecimal basePrice;
    String brand;
    String sku;
    boolean isActive;
    UUID categoryId;
    String categoryName;
    UUID sellerId;
    String sellerName;
    List<ProductVariantResponse> variants;
    List<String> imageUrls;
    Double averageRating;
    Long reviewCount;
    LocalDateTime createdAt;
}