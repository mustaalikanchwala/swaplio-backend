package com.swaplio.swaplio_backend.dto.auth;


import jakarta.validation.constraints.*;

public record LoginRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String password

) {}