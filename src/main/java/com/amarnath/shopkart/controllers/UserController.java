package com.amarnath.shopkart.controllers;

import com.amarnath.shopkart.dto.request.AddressRequest;
import com.amarnath.shopkart.dto.response.AddressResponse;
import com.amarnath.shopkart.dto.response.ApiResponse;
import com.amarnath.shopkart.dto.response.UserResponse;
import com.amarnath.shopkart.security.CustomUserDetails;
import com.amarnath.shopkart.services.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponse response = userService.getUserById(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserResponse request) {

        UserResponse response = userService.updateUser(
                userDetails.getUser().getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    // ===== ADDRESS ENDPOINTS =====

    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse response = userService.addAddress(
                userDetails.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", response));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<AddressResponse> response = userService.getUserAddresses(
                userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {

        AddressResponse response = userService.setDefaultAddress(
                userDetails.getUser().getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Default address updated", response));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {

        userService.deleteAddress(userDetails.getUser().getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
}