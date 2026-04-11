package com.swaplio.swaplio_backend.dto.user;


import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        String fullName,
        String phoneNumber,
        String institution
) {}