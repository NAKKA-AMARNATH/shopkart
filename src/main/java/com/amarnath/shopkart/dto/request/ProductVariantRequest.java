package com.amarnath.shopkart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantRequest {

    String color;
    String size;

    @NotNull(message = "Variant price is required")
    @Positive(message = "Price must be positive")
    BigDecimal price;

    @PositiveOrZero(message = "Stock cannot be negative")
    Integer stockQuantity;

    String imageUrl;
    String sku;
}