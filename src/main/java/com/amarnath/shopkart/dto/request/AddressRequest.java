package com.amarnath.shopkart.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    @NotBlank(message = "Street is required")
    String street;

    @NotBlank(message = "City is required")
    String city;

    @NotBlank(message = "State is required")
    String state;

    @NotBlank(message = "Zip code is required")
    String zipCode;

    @NotBlank(message = "Country is required")
    String country;

    boolean isDefault;
}