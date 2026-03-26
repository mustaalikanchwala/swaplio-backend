// ListingRepository.java
package com.swaplio.swaplio_backend.repository;


import com.swaplio.swaplio_backend.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    // Find all active (not sold, not deleted) listings
    Page<Listing> findByIsSoldFalseAndIsDeletedFalse(Pageable pageable);

    // Search by keyword in title or description
    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.isSold = false " +
            "AND (LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Listing> searchListings(@Param("keyword") String keyword, Pageable pageable);

    // Filter by category
    Page<Listing> findByCategoryIdAndIsSoldFalseAndIsDeletedFalse(
            UUID categoryId, Pageable pageable);

    // Filter by price range
    @Query("SELECT l FROM Listing l WHERE l.isDeleted = false AND l.isSold = false " +
            "AND l.price BETWEEN :minPrice AND :maxPrice")
    Page<Listing> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // Get all listings by a specific seller
    Page<Listing> findBySellerIdAndIsDeletedFalse(UUID sellerId, Pageable pageable);
}