package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByProductId(UUID productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.stockQuantity > 0")
    List<ProductVariant> findInStockVariantsByProductId(@Param("productId") UUID productId);

    @Modifying
    @Query("UPDATE ProductVariant pv SET pv.stockQuantity = pv.stockQuantity - :quantity WHERE pv.id = :id AND pv.stockQuantity >= :quantity")
    int decrementStock(@Param("id") UUID id, @Param("quantity") int quantity);
}