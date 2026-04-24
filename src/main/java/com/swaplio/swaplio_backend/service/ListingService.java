// service/ListingService.java
package com.swaplio.swaplio_backend.service;

import com.swaplio.swaplio_backend.dto.listing.CreateListingRequest;
import com.swaplio.swaplio_backend.dto.listing.ListingImageResponse;
import com.swaplio.swaplio_backend.dto.listing.ListingResponse;
import com.swaplio.swaplio_backend.dto.listing.UpdateListingRequest;
import com.swaplio.swaplio_backend.exception.CategoryNotFoundException;
import com.swaplio.swaplio_backend.exception.InavlidCredentialsException;
import com.swaplio.swaplio_backend.exception.ListingNotFoundException;
import com.swaplio.swaplio_backend.model.Category;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.ListingImage;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.CategoryRepository;
import com.swaplio.swaplio_backend.repository.ListingImageRepository;
import com.swaplio.swaplio_backend.repository.ListingRepository;
import com.swaplio.swaplio_backend.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StorageService storageService;
    private final ListingImageRepository listingImageRepository;

    // ─── CREATE ──────────────────────────────────────────────────────────────
    public ListingResponse createListing(CreateListingRequest request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new InavlidCredentialsException("User not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Listing listing = listingRepository.save(Listing.builder()
                            .seller(seller)
                            .category(category)
                            .title(request.title())
                            .description(request.description())
                            .price(request.price())
                            .condition(request.condition())
                            .build());
        if(request.images()!= null && !request.images().isEmpty()){
            try {
                uploadAndSaveImages(listing,request.images(),0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return toResponse(listing);
    }




    // ─── QUERIES ──────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ListingResponse> getAllListings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return listingRepository.findByIsSoldFalseAndIsDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ListingResponse getListingById(UUID id) {
        return toResponse(listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found")));
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> searchListings(String keyword, UUID categoryId,
                                                String condition,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Listing> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out sold/deleted
            predicates.add(cb.equal(root.get("isSold"), false));
            predicates.add(cb.equal(root.get("isDeleted"), false));

            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like)
                ));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (condition != null && !condition.isBlank()) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return listingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getMyListings(String sellerEmail, int page, int size) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return listingRepository
                .findBySellerIdAndIsDeletedFalse(seller.getId(), pageable)
                .map(this::toResponse);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────
    public ListingResponse updateListing(UUID id, UpdateListingRequest request, String sellerEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));
        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("You can only edit your own listings");
        }
        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> new RuntimeException("Category not found"));

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCondition(request.condition());
        listing.setCategory(category);
        listingRepository.save(listing);

        List<UUID> keepIds = request.keepImageIds();
        List<ListingImage> existingImages = listingImageRepository.findByListingIdOrderByDisplayOrder(listing.getId());
        List<ListingImage> toDelete = existingImages.stream()
                .filter(a -> !keepIds.contains(a.getId()))
                .toList();
        for(ListingImage img : toDelete){
            storageService.deleteImage(img.getImageKey());
            listingImageRepository.delete(img);
        }

        int nxtOrder = keepIds.size();
        if(request.images() != null && !request.images().isEmpty()){
            try {
                uploadAndSaveImages(listing,request.images(),nxtOrder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        recomputePrimary(listing.getId());
        return toResponse(listing);
    }

    // ─── MARK AS SOLD ─────────────────────────────────────────────────────────
    public void markAsSold(UUID id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));
        listing.setSold(true);
        listing.setDeleted(true);       // soft delete
        listing.setDeletedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────
    public void deleteListing(UUID id, String sellerEmail) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));
        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new RuntimeException("You can only delete your own listings");
        }
        listing.setDeleted(true);
        listing.setDeletedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────────────
    private void uploadAndSaveImages(Listing listing, List<MultipartFile> files, int j) throws IOException {
        boolean noExistingPrimary = listingImageRepository
                .findByListingIdOrderByDisplayOrder(listing.getId())
                .stream()
                .noneMatch(ListingImage::isPrimary);

        for (int i = 0; i < files.size(); i++) {
            // Upload to Supabase, returns the storage key (path only)
            String key = storageService.uploadImage(
                    files.get(i),
                    "listings/" + listing.getId()
            );

            listingImageRepository.save(
                    ListingImage.builder()
                            .listing(listing)
                            .imageKey(key)                         // store KEY, never URL
                            .isPrimary(noExistingPrimary && i == 0) // first image = primary if none exists
                            .displayOrder(j + i)
                            .build()
            );
        }
    }

    private void recomputePrimary(UUID listingId) {
        List<ListingImage> remaining =
                listingImageRepository.findByListingIdOrderByDisplayOrder(listingId);

        if (remaining.isEmpty()) return;

        boolean hasPrimary = remaining.stream().anyMatch(ListingImage::isPrimary);
        if (!hasPrimary) {
            ListingImage first = remaining.get(0);
            first.setPrimary(true);
            listingImageRepository.save(first);
        }
    }

    public ListingResponse toResponse(Listing listing) {
        List<ListingImage> images =
                listingImageRepository.findByListingIdOrderByDisplayOrder(listing.getId());

        List<ListingImageResponse> imageDtos = images.stream()
                .map(img -> new ListingImageResponse(
                        img.getId(),
                        storageService.generateSignedUrl(img.getImageKey()), // sign here only
                        img.isPrimary(),
                        img.getDisplayOrder()
                ))
                .toList();

        return ListingResponse.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .price(listing.getPrice())
                .condition(listing.getCondition())
                .isSold(listing.isSold())
                .categoryName(listing.getCategory().getName())
                .sellerName(listing.getSeller().getFullName())
                .sellerId(listing.getSeller().getId())
                .images(imageDtos)
                .createdAt(listing.getCreatedAt())
                .build();
    }

}