package com.amarnath.shopkart.dto.response;

import com.amarnath.shopkart.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    UUID id;
    String email;
    String firstName;
    String lastName;
    String phone;
    Role role;
    boolean isActive;
    LocalDateTime createdAt;
}