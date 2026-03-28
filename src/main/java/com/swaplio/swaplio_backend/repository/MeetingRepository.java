// MeetingRepository.java
package com.swaplio.swaplio_backend.repository;


import com.swaplio.swaplio_backend.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    List<Meeting> findByBuyerId(UUID buyerId);
    List<Meeting> findBySellerId(UUID sellerId);
    List<Meeting> findByListingId(UUID listingId);
}