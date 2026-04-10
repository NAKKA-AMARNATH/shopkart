package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.CreateReviewRequest;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.ReviewResponse;

import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(UUID userId, CreateReviewRequest request);

    ReviewResponse getReviewById(UUID reviewId);

    PagedResponse<ReviewResponse> getProductReviews(UUID productId, int page, int size);

    PagedResponse<ReviewResponse> getUserReviews(UUID userId, int page, int size);

    ReviewResponse updateReview(UUID reviewId, UUID userId, CreateReviewRequest request);

    void deleteReview(UUID reviewId, UUID userId);
}