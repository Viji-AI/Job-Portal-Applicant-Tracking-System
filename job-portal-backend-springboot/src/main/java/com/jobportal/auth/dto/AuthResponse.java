package com.jobportal.auth.dto;

public record AuthResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {}
