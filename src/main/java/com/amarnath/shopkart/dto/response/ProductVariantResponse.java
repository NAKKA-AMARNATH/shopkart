package com.amarnath.shopkart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantResponse {

    UUID id;
    String color;
    String size;
    BigDecimal price;
    Integer stockQuantity;
    String imageUrl;
    String sku;
}