package com.swaplio.swaplio_backend.dto.auth;


public record AuthResponse(
        String token,
        String email,
        String fullName
) {}