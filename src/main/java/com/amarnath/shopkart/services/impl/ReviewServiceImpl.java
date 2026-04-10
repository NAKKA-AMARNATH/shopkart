package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.dto.request.CreateReviewRequest;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.ReviewResponse;
import com.amarnath.shopkart.entities.Product;
import com.amarnath.shopkart.entities.Review;
import com.amarnath.shopkart.entities.User;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.OrderItemRepository;
import com.amarnath.shopkart.repositories.ProductRepository;
import com.amarnath.shopkart.repositories.ReviewRepository;
import com.amarnath.shopkart.repositories.UserRepository;
import com.amarnath.shopkart.services.interfaces.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {

    ReviewRepository reviewRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    OrderItemRepository orderItemRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {

        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new BusinessException("You have already reviewed this product");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + request.getProductId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId));

        boolean isVerified = orderItemRepository
                .hasUserPurchasedProduct(userId, request.getProductId());

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .isVerified(isVerified)
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(UUID reviewId) {
        Review review = findReviewById(reviewId);
        return mapToResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getProductReviews(
            UUID productId, int page, int size) {

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);

        return mapToPagedResponse(reviewPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getUserReviews(
            UUID userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByUserId(userId, pageable);

        return mapToPagedResponse(reviewPage);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(
            UUID reviewId, UUID userId, CreateReviewRequest request) {

        Review review = findReviewById(reviewId);

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to update this review");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setBody(request.getBody());

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        Review review = findReviewById(reviewId);

        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    // ===== PRIVATE HELPER METHODS =====

    private Review findReviewById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id: " + reviewId));
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName()
                        + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .title(review.getTitle())
                .body(review.getBody())
                .isVerified(review.isVerified())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private PagedResponse<ReviewResponse> mapToPagedResponse(Page<Review> page) {
        return PagedResponse.<ReviewResponse>builder()
                .content(page.getContent().stream()
                        .map(this::mapToResponse)
                        .toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}