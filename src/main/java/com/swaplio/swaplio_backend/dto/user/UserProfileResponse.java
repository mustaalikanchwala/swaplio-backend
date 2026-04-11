package com.swaplio.swaplio_backend.dto.user;


import lombok.Builder;

import java.util.UUID;
@Builder
public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String institution,
        boolean isVerified
) {}