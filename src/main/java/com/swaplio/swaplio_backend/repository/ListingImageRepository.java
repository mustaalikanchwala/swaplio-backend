package com.swaplio.swaplio_backend.repository;

// repository/ListingImageRepository.java

import com.swaplio.swaplio_backend.model.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {
    List<ListingImage> findByListingIdOrderByDisplayOrder(UUID listingId);
}
