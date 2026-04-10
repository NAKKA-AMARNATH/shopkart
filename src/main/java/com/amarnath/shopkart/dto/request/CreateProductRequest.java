package com.amarnath.shopkart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    String name;

    String description;

    @NotNull(message = "Base price is required")
    @Positive(message = "Price must be positive")
    BigDecimal basePrice;

    String brand;

    String sku;

    @NotNull(message = "Category is required")
    UUID categoryId;

    List<ProductVariantRequest> variants;

    List<String> imageUrls;
}