package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.dto.response.DashboardStatsResponse;
import com.amarnath.shopkart.dto.response.OrderResponse;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.UserResponse;
import com.amarnath.shopkart.enums.OrderStatus;
import com.amarnath.shopkart.repositories.*;
import com.amarnath.shopkart.services.interfaces.AdminService;
import com.amarnath.shopkart.services.interfaces.OrderService;
import com.amarnath.shopkart.services.interfaces.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminServiceImpl implements AdminService {

    UserRepository userRepository;
    ProductRepository productRepository;
    OrderRepository orderRepository;
    PaymentRepository paymentRepository;
    UserService userService;
    OrderService orderService;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(
            LocalDateTime start, LocalDateTime end) {

        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();
        Long totalOrders = orderRepository.count();

        Long totalOrdersInRange = orderRepository
                .countOrdersBetween(start, end);

        Double totalRevenueInRange = orderRepository
                .sumTotalByStatusAndDateRange(OrderStatus.DELIVERED, start, end);

        Long pendingOrders = orderRepository
                .findByStatus(OrderStatus.PENDING, PageRequest.of(0, 1))
                .getTotalElements();

        Long deliveredOrders = orderRepository
                .findByStatus(OrderStatus.DELIVERED, PageRequest.of(0, 1))
                .getTotalElements();

        Long cancelledOrders = orderRepository
                .findByStatus(OrderStatus.CANCELLED, PageRequest.of(0, 1))
                .getTotalElements();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalOrdersInRange(totalOrdersInRange)
                .totalRevenueInRange(totalRevenueInRange != null
                        ? totalRevenueInRange : 0.0)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .cancelledOrders(cancelledOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return PagedResponse.<UserResponse>builder()
                .content(userRepository.findAll(pageable)
                        .getContent()
                        .stream()
                        .map(user -> userService.getUserById(user.getId()))
                        .toList())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(userRepository.count())
                .totalPages((int) Math.ceil((double) userRepository.count() / size))
                .last(page >= (int) Math.ceil((double) userRepository.count() / size) - 1)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(int page, int size) {
        return orderService.getAllOrders(page, size);
    }
}