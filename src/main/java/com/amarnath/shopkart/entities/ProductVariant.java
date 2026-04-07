package com.amarnath.shopkart.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    String color;

    String size;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity;

    @Column(name = "image_url")
    String imageUrl;

    @Column(unique = true)
    String sku;

}

