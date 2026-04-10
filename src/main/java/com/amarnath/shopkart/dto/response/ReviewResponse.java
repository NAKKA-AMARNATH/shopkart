package com.amarnath.shopkart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {

    UUID id;
    UUID productId;
    String productName;
    UUID userId;
    String userName;
    Integer rating;
    String title;
    String body;
    boolean isVerified;
    LocalDateTime createdAt;
}