package com.swaplio.swaplio_backend.dto.listing;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ListingResponse(

        UUID id,
        String title,
        String description,
        BigDecimal price,
        String condition,
        boolean isSold,
        String categoryName,
        String sellerName,
        UUID sellerId,
        List<ListingImageResponse> images,
        LocalDateTime createdAt

) {}