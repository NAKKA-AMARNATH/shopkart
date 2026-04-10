package com.amarnath.shopkart.dto.response;

import com.amarnath.shopkart.enums.OrderStatus;
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
public class OrderResponse {

    UUID id;
    String orderNumber;
    OrderStatus status;
    List<OrderItemResponse> items;
    UUID shippingAddressId;
    String shippingAddress;
    BigDecimal subtotal;
    BigDecimal tax;
    BigDecimal shippingFee;
    BigDecimal total;
    String paymentMethod;
    String paymentStatus;
    LocalDateTime createdAt;
}