package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.response.DashboardStatsResponse;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.OrderResponse;
import com.amarnath.shopkart.dto.response.UserResponse;

import java.time.LocalDateTime;

public interface AdminService {

    DashboardStatsResponse getDashboardStats(LocalDateTime start, LocalDateTime end);

    PagedResponse<UserResponse> getAllUsers(int page, int size);

    PagedResponse<OrderResponse> getAllOrders(int page, int size);
}