package com.hospital.hms.service;

import com.hospital.hms.dto.request.LoginRequest;
import com.hospital.hms.dto.request.RegisterRequest;
import com.hospital.hms.dto.response.ApiResponse;
import com.hospital.hms.dto.response.JwtResponse;

public interface AuthService {

    JwtResponse authenticateUser(LoginRequest loginRequest);

    ApiResponse<String> registerUser(RegisterRequest registerRequest);
}
