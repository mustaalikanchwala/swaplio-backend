package com.swaplio.swaplio_backend.dto.listing;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateListingRequest(

        @NotBlank
        String title,

        String description,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal price,

        @NotNull
        UUID categoryId,

        // "NEW", "LIKE_NEW", "GOOD", "FAIR"
        String condition

) {}