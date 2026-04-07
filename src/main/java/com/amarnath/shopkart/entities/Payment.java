package com.amarnath.shopkart.entities;

import com.amarnath.shopkart.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment extends BaseEntity{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Order order;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    String gatewayResponse;

    @Column(name = "paid_at")
    LocalDateTime paidAt;
}

