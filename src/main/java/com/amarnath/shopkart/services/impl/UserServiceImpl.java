package com.amarnath.shopkart.services.impl;

import com.amarnath.shopkart.config.CacheConfig;
import com.amarnath.shopkart.dto.request.AddressRequest;
import com.amarnath.shopkart.dto.response.AddressResponse;
import com.amarnath.shopkart.dto.response.UserResponse;
import com.amarnath.shopkart.entities.Address;
import com.amarnath.shopkart.entities.User;
import com.amarnath.shopkart.exceptions.BusinessException;
import com.amarnath.shopkart.exceptions.ResourceNotFoundException;
import com.amarnath.shopkart.repositories.AddressRepository;
import com.amarnath.shopkart.repositories.UserRepository;
import com.amarnath.shopkart.services.interfaces.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    AddressRepository addressRepository;

    // ── READ ───────────────────────────────────────────────────────────────────
    @Override
    @Cacheable(value = CacheConfig.CACHE_USERS, key = "#id")
    public UserResponse getUserById(UUID id) {
        return mapToUserResponse(findUserById(id));
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_USERS, key = "'email:' + #email")
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #userId")
    public List<AddressResponse> getUserAddresses(UUID userId) {
        findUserById(userId);
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToAddressResponse)
                .toList();
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = CacheConfig.CACHE_USERS, key = "#id")
            },
            evict = {
                    // email-keyed entry is now stale — we don't know the email here so wipe all
                    @CacheEvict(value = CacheConfig.CACHE_USERS, allEntries = true)
            }
    )
    public UserResponse updateUser(UUID id, UserResponse userResponse) {
        User user = findUserById(id);
        user.setFirstName(userResponse.getFirstName());
        user.setLastName(userResponse.getLastName());
        user.setPhone(userResponse.getPhone());
        return mapToUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "#userId"),
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #userId")
    })
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        User user = findUserById(userId);

        if (request.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(existing -> {
                        existing.setDefault(false);
                        addressRepository.save(existing);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        return mapToAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #userId")
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        findUserById(userId);

        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(existing -> {
                    existing.setDefault(false);
                    addressRepository.save(existing);
                });

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Address does not belong to this user");
        }

        address.setDefault(true);
        return mapToAddressResponse(addressRepository.save(address));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_USERS, allEntries = true)
    })
    public void deleteUser(UUID id) {
        userRepository.delete(findUserById(id));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #id")
    })
    public void deactivateUser(UUID id) {
        User user = findUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "#id"),
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #id")
    })
    public void activateUser(UUID id) {
        User user = findUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_USERS, key = "'addresses:' + #userId")
    public void deleteAddress(UUID userId, UUID addressId) {
        findUserById(userId);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Address does not belong to this user");
        }

        addressRepository.delete(address);
    }

    // ── PRIVATE HELPERS ────────────────────────────────────────────────────────
    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isDefault(address.isDefault())
                .build();
    }
}