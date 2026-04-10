package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.AddressRequest;
import com.amarnath.shopkart.dto.response.AddressResponse;
import com.amarnath.shopkart.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(UUID id, UserResponse userResponse);

    void deleteUser(UUID id);

    void deactivateUser(UUID id);

    void activateUser(UUID id);

    AddressResponse addAddress(UUID userId, AddressRequest request);

    List<AddressResponse> getUserAddresses(UUID userId);

    AddressResponse setDefaultAddress(UUID userId, UUID addressId);

    void deleteAddress(UUID userId, UUID addressId);
}