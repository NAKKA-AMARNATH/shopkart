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
public class OrderItemResponse {

    UUID id;
    UUID productVariantId;
    String productName;
    String variantColor;
    String variantSize;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
}