package com.amarnath.shopkart.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {

    UUID id;
    String name;
    String slug;
    String description;
    String imageUrl;
    boolean isActive;
    UUID parentId;
    String parentName;
    List<CategoryResponse> children;
}