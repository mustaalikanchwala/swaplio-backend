// MeetingRepository.java
package com.swaplio.swaplio_backend.repository;


import com.swaplio.swaplio_backend.dto.meeting.MeetingStatus;
import com.swaplio.swaplio_backend.model.Meeting;
import com.swaplio.swaplio_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    List<Meeting> findByBuyerId(UUID buyerId);
    List<Meeting> findBySellerId(UUID sellerId);
    List<Meeting> findByListingId(UUID listingId);

    boolean existsByBuyerAndListingIdAndStatusIn(User buyer, UUID id, List<MeetingStatus> status);

    List<Meeting> findByBuyerOrderByCreatedAtDesc(User user);

    List<Meeting> findBySellerOrderByCreatedAtDesc(User seller);

    // ── Buyer queries ─────────────────────────────────────────────────────────

    @Query("""
            SELECT m FROM Meeting m
            WHERE m.buyer = :buyer
              AND (:status IS NULL OR m.status = :status)
              AND (CAST(:startDate AS localDate) IS NULL OR m.meetingDate >= :startDate)
              AND (CAST(:endDate AS localDate)   IS NULL OR m.meetingDate <= :endDate)
            ORDER BY m.createdAt DESC
            """)
    List<Meeting> findByBuyerFiltered(
            @Param("buyer")     User buyer,
            @Param("status")    MeetingStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );

    // ── Seller queries ────────────────────────────────────────────────────────

    @Query("""
            SELECT m FROM Meeting m
            WHERE m.seller = :seller
              AND (:status IS NULL OR m.status = :status)
              AND (CAST(:startDate AS localDate) IS NULL OR m.meetingDate >= :startDate)
              AND (CAST(:endDate AS localDate)   IS NULL OR m.meetingDate <= :endDate)
            ORDER BY m.createdAt DESC
            """)
    List<Meeting> findBySellerFiltered(
            @Param("seller")    User seller,
            @Param("status")    MeetingStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate
    );
}