package com.amarnath.shopkart.controllers;

import com.amarnath.shopkart.dto.request.AddToCartRequest;
import com.amarnath.shopkart.dto.response.ApiResponse;
import com.amarnath.shopkart.dto.response.CartResponse;
import com.amarnath.shopkart.security.CustomUserDetails;
import com.amarnath.shopkart.services.interfaces.CartService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {

    CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponse response = cartService.getCart(
                userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addToCart(
                userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID cartItemId,
            @RequestParam int quantity) {

        CartResponse response = cartService.updateCartItem(
                userDetails.getUser().getId(), cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID cartItemId) {

        CartResponse response = cartService.removeFromCart(
                userDetails.getUser().getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        cartService.clearCart(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}