package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.AddToCartRequest;
import com.amarnath.shopkart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID userId);

    CartResponse addToCart(UUID userId, AddToCartRequest request);

    CartResponse updateCartItem(UUID userId, UUID cartItemId, int quantity);

    CartResponse removeFromCart(UUID userId, UUID cartItemId);

    void clearCart(UUID userId);
}