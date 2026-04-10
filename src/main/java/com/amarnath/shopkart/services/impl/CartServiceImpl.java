package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.config.CacheConfig;
import com.amarnath.shopkart.dto.request.AddToCartRequest;
import com.amarnath.shopkart.dto.response.CartItemResponse;
import com.amarnath.shopkart.dto.response.CartResponse;
import com.amarnath.shopkart.entities.Cart;
import com.amarnath.shopkart.entities.CartItem;
import com.amarnath.shopkart.entities.ProductVariant;
import com.amarnath.shopkart.entities.User;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.CartItemRepository;
import com.amarnath.shopkart.repositories.CartRepository;
import com.amarnath.shopkart.repositories.ProductVariantRepository;
import com.amarnath.shopkart.repositories.UserRepository;
import com.amarnath.shopkart.services.interfaces.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductVariantRepository productVariantRepository;
    UserRepository userRepository;
    RedisTemplate<String, Object> redisTemplate;

    // ── READ ───────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        String key = cartKey(userId);

        // 1. Check Redis first
        CartResponse cached = (CartResponse) redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        // 2. Cache miss — fetch from DB
        Cart cart = cartRepository.findByUserIdWithItems(userId).orElse(null);

        CartResponse response;
        if (cart == null) {
            response = CartResponse.builder()
                    .userId(userId)
                    .items(List.of())
                    .totalItems(0)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        } else {
            response = mapToResponse(cart);
        }

        // 3. Store in Redis
        redisTemplate.opsForValue().set(key, response, Duration.ofDays(7));
        return response;
    }

    // ── WRITE (add) ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CartResponse addToCart(UUID userId, AddToCartRequest request) {
        ProductVariant variant = productVariantRepository
                .findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Insufficient stock. Available: "
                    + variant.getStockQuantity());
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        CartItem existingItem = cartItemRepository
                .findByCartIdAndProductVariantId(cart.getId(), request.getProductVariantId())
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > variant.getStockQuantity()) {
                throw new BusinessException("Insufficient stock. Available: "
                        + variant.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(variant.getPrice())
                    .build();
            cartItemRepository.save(newItem);
        }

        // Fetch fresh state, write to cache, return
        CartResponse response = mapToResponse(
                cartRepository.findByUserIdWithItems(userId).orElseThrow());
        redisTemplate.opsForValue().set(cartKey(userId), response, Duration.ofDays(7));
        return response;
    }

    // ── WRITE (update quantity) ────────────────────────────────────────────────
    @Override
    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID cartItemId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("Cart item does not belong to this cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            if (quantity > cartItem.getProductVariant().getStockQuantity()) {
                throw new BusinessException("Insufficient stock. Available: "
                        + cartItem.getProductVariant().getStockQuantity());
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        // Fetch fresh state, write to cache, return
        CartResponse response = mapToResponse(
                cartRepository.findByUserIdWithItems(userId).orElseThrow());
        redisTemplate.opsForValue().set(cartKey(userId), response, Duration.ofDays(7));
        return response;
    }

    // ── WRITE (remove item) ────────────────────────────────────────────────────
    @Override
    @Transactional
    public CartResponse removeFromCart(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("Cart item does not belong to this cart");
        }

        cartItemRepository.delete(cartItem);

        // Fetch fresh state, write to cache, return
        CartResponse response = mapToResponse(
                cartRepository.findByUserIdWithItems(userId).orElseThrow());
        redisTemplate.opsForValue().set(cartKey(userId), response, Duration.ofDays(7));
        return response;
    }

    // ── CLEAR ──────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());

        // Delete from Redis entirely
        redisTemplate.delete(cartKey(userId));
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────
    private String cartKey(UUID userId) {
        return CacheConfig.CACHE_CART + "::" + userId;
    }

    private Cart createNewCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));
        return cartRepository.save(Cart.builder().user(user).build());
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems()
                .stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productVariantId(item.getProductVariant().getId())
                        .productName(item.getProductVariant().getProduct().getName())
                        .variantColor(item.getProductVariant().getColor())
                        .variantSize(item.getProductVariant().getSize())
                        .imageUrl(item.getProductVariant().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getUnitPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .totalAmount(totalAmount)
                .build();
    }
}