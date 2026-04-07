package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(UUID userId);

    @Query("SELECT c FROM Cart c JOIN FETCH c.items ci JOIN FETCH ci.productVariant pv JOIN FETCH pv.product WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);
}