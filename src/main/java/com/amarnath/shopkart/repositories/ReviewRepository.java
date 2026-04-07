package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByProductId(UUID productId, Pageable pageable);

    Page<Review> findByUserId(UUID userId, Pageable pageable);

    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    boolean existsByProductIdAndUserId(UUID productId, UUID userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.rating = :rating")
    Long countByProductIdAndRating(@Param("productId") UUID productId,
                                   @Param("rating") Integer rating);
}