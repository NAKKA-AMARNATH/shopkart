package com.amarnath.shopkart.repositories;

import com.amarnath.shopkart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndProductVariantId(UUID cartId, UUID productVariantId);

    void deleteByCartId(UUID cartId);
}