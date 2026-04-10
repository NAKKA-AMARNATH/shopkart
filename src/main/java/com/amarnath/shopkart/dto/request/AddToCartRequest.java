package com.amarnath.shopkart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartRequest {

    @NotNull(message = "Product variant is required")
    UUID productVariantId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be at least 1")
    Integer quantity;
}