// controller/ListingController.java
package com.swaplio.swaplio_backend.controller;

import com.swaplio.swaplio_backend.dto.listing.CreateListingRequest;
import com.swaplio.swaplio_backend.dto.listing.ListingResponse;
import com.swaplio.swaplio_backend.dto.listing.UpdateListingRequest;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.ListingImage;
import com.swaplio.swaplio_backend.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // GET /api/listings?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<ListingResponse>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listingService.getAllListings(page, size));
    }

    // GET /api/listings/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable UUID id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    // GET /api/listings/search?keyword=physics&categoryId=...&minPrice=50&maxPrice=500
    @GetMapping("/search")
    public ResponseEntity<Page<ListingResponse>> searchListings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                listingService.searchListings(keyword, categoryId, minPrice, maxPrice, page, size));
    }

    // GET /api/listings/my?page=0&size=10
    @GetMapping("/my")
    public ResponseEntity<Page<ListingResponse>> getMyListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {
        return ResponseEntity.ok(listingService.getMyListings(auth.getName(), page, size));
    }

    // POST /api/listings  (requires login)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> createListing(
            @Valid @ModelAttribute CreateListingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(listingService.createListing(request, auth.getName()));
    }

    // PUT /api/listings/{id}  (requires login)
    @PutMapping(value = "/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateListingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(listingService.updateListing(id, request, auth.getName()));
    }

    // PATCH /api/listings/{id}/sold  (marks as sold + auto soft deletes)
    @PatchMapping("/{id}/sold")
    public ResponseEntity<String> markAsSold(@PathVariable UUID id) {
        listingService.markAsSold(id);
        return ResponseEntity.ok("Listing marked as sold");
    }

    // DELETE /api/listings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteListing(
            @PathVariable UUID id,
            Authentication auth) {
        listingService.deleteListing(id, auth.getName());
        return ResponseEntity.ok("Listing deleted");
    }

}