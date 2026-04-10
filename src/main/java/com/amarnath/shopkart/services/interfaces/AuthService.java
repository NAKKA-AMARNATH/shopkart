package com.amarnath.shopkart.services.interfaces;

import com.amarnath.shopkart.dto.request.LoginRequest;
import com.amarnath.shopkart.dto.request.RegisterRequest;
import com.amarnath.shopkart.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}