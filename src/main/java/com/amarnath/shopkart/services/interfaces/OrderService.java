package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.CreateOrderRequest;
import com.amarnath.shopkart.dto.response.OrderResponse;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.enums.OrderStatus;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(UUID userId, CreateOrderRequest request);

    OrderResponse getOrderById(UUID orderId, UUID userId);

    OrderResponse getOrderByOrderNumber(String orderNumber, UUID userId);

    PagedResponse<OrderResponse> getUserOrders(UUID userId, int page, int size);

    PagedResponse<OrderResponse> getUserOrdersByStatus(
            UUID userId, OrderStatus status, int page, int size);

    OrderResponse updateOrderStatus(UUID orderId, OrderStatus status);

    PagedResponse<OrderResponse> getAllOrders(int page, int size);

    void cancelOrder(UUID orderId, UUID userId);
}