package com.amarnath.shopkart.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImage extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(name = "image_url", nullable = false)
    String imageUrl;

    @Column(name = "is_primary")
    boolean isPrimary = false;

    @Column(name = "sort_order")
    Integer sortOrder = 0;
}

