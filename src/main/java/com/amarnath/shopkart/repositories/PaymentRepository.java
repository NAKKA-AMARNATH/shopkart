package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.Payment;
import com.amarnath.shopkart.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId AND p.status = :status")
    java.util.List<Payment> findByUserIdAndStatus(@Param("userId") UUID userId,
                                                  @Param("status") PaymentStatus status);
}