package com.amarnath.shopkart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardStatsResponse {

    Long totalUsers;
    Long totalProducts;
    Long totalOrders;
    Long totalOrdersInRange;
    Double totalRevenueInRange;
    Long pendingOrders;
    Long deliveredOrders;
    Long cancelledOrders;
}