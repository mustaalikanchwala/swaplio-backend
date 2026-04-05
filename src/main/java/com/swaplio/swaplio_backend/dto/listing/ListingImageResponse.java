// dto/listing/ListingImageResponse.java
package com.swaplio.swaplio_backend.dto.listing;


import java.util.UUID;

public record ListingImageResponse(
        UUID id,
        String signedUrl,      // generated on-demand, valid 1 hour — never stored
        boolean isPrimary,
        int displayOrder
) {}