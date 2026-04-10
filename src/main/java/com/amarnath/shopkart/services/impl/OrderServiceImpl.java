package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.dto.request.CreateOrderRequest;
import com.amarnath.shopkart.dto.response.OrderItemResponse;
import com.amarnath.shopkart.dto.response.OrderResponse;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.entities.*;
import com.amarnath.shopkart.enums.OrderStatus;
import com.amarnath.shopkart.enums.PaymentStatus;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.*;
import com.amarnath.shopkart.services.interfaces.OrderService;
import com.amarnath.shopkart.utils.AppConstants;
import com.amarnath.shopkart.utils.OrderNumberGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    AddressRepository addressRepository;
    PaymentRepository paymentRepository;
    ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {

        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot place order with empty cart");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + request.getAddressId()));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Address does not belong to this user");
        }

        // Calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            subtotal = subtotal.add(
                    cartItem.getUnitPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // Calculate tax
        BigDecimal tax = subtotal
                .multiply(BigDecimal.valueOf(AppConstants.TAX_RATE))
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate shipping fee
        BigDecimal shippingFee = subtotal.compareTo(
                AppConstants.FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : AppConstants.SHIPPING_FEE;

        // Calculate total
        BigDecimal total = subtotal.add(tax).add(shippingFee);

        // Generate unique order number
        String orderNumber = OrderNumberGenerator.generate();
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            orderNumber = OrderNumberGenerator.generate();
        }

        // Create order
        Order order = Order.builder()
                .user(address.getUser())
                .shippingAddress(address)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .tax(tax)
                .shippingFee(shippingFee)
                .total(total)
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items and decrement stock
        for (CartItem cartItem : cart.getItems()) {
            int updated = productVariantRepository.decrementStock(
                    cartItem.getProductVariant().getId(),
                    cartItem.getQuantity());

            if (updated == 0) {
                throw new BusinessException(
                        "Insufficient stock for: "
                                + cartItem.getProductVariant().getProduct().getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .productVariant(cartItem.getProductVariant())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(cartItem.getUnitPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();

            savedOrder.getItems().add(orderItemRepository.save(orderItem));
        }

        // Create payment record
        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .amount(total)
                .build();

        paymentRepository.save(payment);

        // Clear the cart
        cartItemRepository.deleteByCartId(cart.getId());

        return mapToResponse(savedOrder, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return mapToResponse(order, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findAll(pageable);

        return PagedResponse.<OrderResponse>builder()
                .content(orderPage.getContent().stream()
                        .map(o -> mapToResponse(o,
                                paymentRepository.findByOrderId(o.getId()).orElse(null)))
                        .toList())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, UUID userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with number: " + orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to view this order");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        return mapToResponse(order, payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getUserOrders(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        return PagedResponse.<OrderResponse>builder()
                .content(orderPage.getContent().stream()
                        .map(o -> mapToResponse(o,
                                paymentRepository.findByOrderId(o.getId()).orElse(null)))
                        .toList())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getUserOrdersByStatus(
            UUID userId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByUserIdAndStatus(userId, status, pageable);

        return PagedResponse.<OrderResponse>builder()
                .content(orderPage.getContent().stream()
                        .map(o -> mapToResponse(o,
                                paymentRepository.findByOrderId(o.getId()).orElse(null)))
                        .toList())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        order.setStatus(status);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        return mapToResponse(orderRepository.save(order), payment);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to cancel this order");
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.OUT_FOR_DELIVERY ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getProductVariant();
            variant.setStockQuantity(
                    variant.getStockQuantity() + item.getQuantity());
            productVariantRepository.save(variant);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // ===== PRIVATE HELPER METHODS =====

    private OrderResponse mapToResponse(Order order, Payment payment) {
        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productVariantId(item.getProductVariant().getId())
                        .productName(item.getProductVariant().getProduct().getName())
                        .variantColor(item.getProductVariant().getColor())
                        .variantSize(item.getProductVariant().getSize())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .toList();

        Address addr = order.getShippingAddress();
        String fullAddress = addr.getStreet() + ", " + addr.getCity()
                + ", " + addr.getState() + " - " + addr.getZipCode()
                + ", " + addr.getCountry();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .items(itemResponses)
                .shippingAddressId(addr.getId())
                .shippingAddress(fullAddress)
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingFee(order.getShippingFee())
                .total(order.getTotal())
                .paymentMethod(payment != null ? payment.getPaymentMethod() : null)
                .paymentStatus(payment != null ? payment.getStatus().name() : null)
                .createdAt(order.getCreatedAt())
                .build();
    }
}