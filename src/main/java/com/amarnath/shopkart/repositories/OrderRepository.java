package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.Order;
import com.amarnath.shopkart.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(UUID userId, OrderStatus status, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o JOIN FETCH o.items oi JOIN FETCH oi.productVariant pv JOIN FETCH pv.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :start AND :end")
    Double sumTotalByStatusAndDateRange(@Param("status") OrderStatus status,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}