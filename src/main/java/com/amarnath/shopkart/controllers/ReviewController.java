package com.amarnath.shopkart.controllers;

import com.amarnath.shopkart.dto.request.CreateReviewRequest;
import com.amarnath.shopkart.dto.response.ApiResponse;
import com.amarnath.shopkart.dto.response.PagedResponse;
import com.amarnath.shopkart.dto.response.ReviewResponse;
import com.amarnath.shopkart.security.CustomUserDetails;
import com.amarnath.shopkart.services.interfaces.ReviewService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.createReview(
                userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", response));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(
            @PathVariable UUID reviewId) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getReviewById(reviewId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getProductReviews(productId, page, size)));
    }

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getUserReviews(
                        userDetails.getUser().getId(), page, size)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.updateReview(
                reviewId, userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", response));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID reviewId) {

        reviewService.deleteReview(reviewId, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully", null));
    }
}