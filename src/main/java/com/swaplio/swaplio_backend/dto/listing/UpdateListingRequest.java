package com.swaplio.swaplio_backend.dto.listing;


import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateListingRequest(

        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must be positive")
        BigDecimal price,

        @NotBlank(message = "Condition is required")
        String condition,

        @NotNull(message = "Category is required")
        UUID categoryId,

        // IDs of existing images to KEEP
        List<UUID> keepImageIds,

        // New images to upload
        List<MultipartFile> images

) {}