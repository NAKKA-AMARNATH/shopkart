package com.amarnath.shopkart.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.UUID;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {

    UUID id;
    String street;
    String city;
    String state;
    String zipCode;
    String country;
    boolean isDefault;
}