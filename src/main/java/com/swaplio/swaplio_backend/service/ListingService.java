// service/ListingService.java
package com.swaplio.swaplio_backend.service;

import com.swaplio.swaplio_backend.dto.listing.CreateListingRequest;
import com.swaplio.swaplio_backend.model.Category;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.CategoryRepository;
import com.swaplio.swaplio_backend.repository.ListingRepository;
import com.swaplio.swaplio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Listing createListing(CreateListingRequest request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Listing listing = Listing.builder()
                .seller(seller)
                .category(category)
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .condition(request.condition())
                .build();
        return listingRepository.save(listing);
    }

    public Page<Listing> getAllListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return listingRepository.findByIsSoldFalseAndIsDeletedFalse(pageable);
    }

    public Listing getListingById(UUID id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
    }

    public Page<Listing> searchListings(String keyword, UUID categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword != null && !keyword.isEmpty()) {
            return listingRepository.searchListings(keyword, pageable);
        }
        if (categoryId != null) {
            return listingRepository.findByCategoryIdAndIsSoldFalseAndIsDeletedFalse(categoryId, pageable);
        }
        if (minPrice != null && maxPrice != null) {
            return listingRepository.findByPriceRange(minPrice, maxPrice, pageable);
        }
        return getAllListings(page, size);
    }

    public Listing updateListing(UUID id, CreateListingRequest request, String sellerEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("You can only edit your own listings");
        }
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCondition(request.condition());
        return listingRepository.save(listing);
    }

    public void markAsSold(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        listing.setSold(true);
        listing.setDeleted(true);       // soft delete
        listing.setDeletedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    public void deleteListing(UUID id, String sellerEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("You can only delete your own listings");
        }
        listing.setDeleted(true);
        listing.setDeletedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }
}