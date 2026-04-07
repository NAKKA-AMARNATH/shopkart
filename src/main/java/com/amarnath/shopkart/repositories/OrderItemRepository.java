package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.productVariant.product.id = :productId")
    List<OrderItem> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.id = :userId " +
            "AND oi.productVariant.product.id = :productId " +
            "AND o.status = 'DELIVERED'")
    boolean hasUserPurchasedProduct(@Param("userId") UUID userId,
                                    @Param("productId") UUID productId);
}