// MeetingRepository.java
package com.swaplio.swaplio_backend.repository;


import com.swaplio.swaplio_backend.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    List<Meeting> findByBuyerId(UUID buyerId);
    List<Meeting> findBySellerId(UUID sellerId);
    List<Meeting> findByListingId(UUID listingId);
}