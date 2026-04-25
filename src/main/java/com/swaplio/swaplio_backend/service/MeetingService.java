package com.swaplio.swaplio_backend.service;

// service/MeetingService.java
import com.swaplio.swaplio_backend.dto.meeting.CreateMeetingRequest;
import com.swaplio.swaplio_backend.dto.meeting.MeetingResponse;
import com.swaplio.swaplio_backend.dto.meeting.MeetingStatus;
import com.swaplio.swaplio_backend.dto.meeting.SellerRespondRequest;
import com.swaplio.swaplio_backend.exception.InavlidCredentialsException;
import com.swaplio.swaplio_backend.exception.ListingNotFoundException;
import com.swaplio.swaplio_backend.exception.MeetingNotFoundException;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.Meeting;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.ListingRepository;
import com.swaplio.swaplio_backend.repository.MeetingRepository;
import com.swaplio.swaplio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.TenantId;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // BUYER: Request a meeting
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a PENDING meeting request from buyer → seller.
     * Prevents duplicate active requests for the same listing.
     */
    @Transactional
    public MeetingResponse requestMeeting(CreateMeetingRequest request, String buyerEmail) {

        User buyer = findUserByEmail(buyerEmail);

        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        // Prevent requesting a meeting on your own listing
        if (listing.getSeller().getEmail().equals(buyerEmail)) {
            throw new IllegalStateException("You cannot request a meeting on your own listing");
        }

        // Prevent duplicate pending / rescheduled requests
        boolean alreadyActive = meetingRepository.existsByBuyerAndListingIdAndStatusIn(
                buyer,
                listing.getId(),
                List.of(MeetingStatus.PENDING, MeetingStatus.RESCHEDULED)
        );
        if (alreadyActive) {
            throw new IllegalStateException(
                    "You already have an active meeting request for this listing");
        }

        Meeting meeting = Meeting.builder()
                .listing(listing)
                .buyer(buyer)
                .seller(listing.getSeller())
                .meetingDate(request.meetingDate())
                .meetingTime(request.meetingTime())
                .location(request.location())
                .notes(request.notes())
                .status(MeetingStatus.PENDING)
                .build();

        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SELLER: Respond to a PENDING request
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Seller can:
     *   CONFIRMED   → accept the buyer's original time
     *   REJECTED    → decline the request
     *   RESCHEDULED → propose a new date/time for the buyer to review
     */
    @Transactional
    public MeetingResponse sellerRespond(
            UUID meetingId,
            SellerRespondRequest request,
            String sellerEmail ){

        Meeting meeting = findMeeting(meetingId);
        asserSeller(meeting,sellerEmail);

        // Seller can only respond when meeting is PENDING
        if (meeting.getStatus() != MeetingStatus.PENDING) {
            throw new IllegalStateException(
                    "You can only respond to PENDING meeting requests. Current status: "
                            + meeting.getStatus());
        }

        switch(request.action()){
            case CONFIRMED -> meeting.setStatus(MeetingStatus.CONFIRMED);

            case REJECTED -> meeting.setStatus(MeetingStatus.REJECTED);

            case RESCHEDULED -> {
                if(request.proposedTime() == null || request.proposedDate() == null){
                    throw new IllegalArgumentException(
                            "proposedDate and proposedTime are required when rescheduling");
                }
                meeting.setStatus(MeetingStatus.RESCHEDULED);
                meeting.setProposedDate(request.proposedDate());
                meeting.setProposedTime(request.proposedTime());
                // fall back to original location if seller didn't specify one
                meeting.setProposedLocation(
                        request.proposedLocation() != null
                                ? request.proposedLocation()
                                : meeting.getLocation()
                );
                meeting.setSellerNote(request.sellerNote());
            }

            default -> throw new IllegalArgumentException(
                    "Invalid action. Allowed: CONFIRMED, REJECTED, RESCHEDULED");
        }
        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    public List<Meeting> getMyMeetingsAsBuyer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InavlidCredentialsException("User not found"));
        return meetingRepository.findByBuyerId(user.getId());
    }

    public List<Meeting> getMyMeetingsAsSeller(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InavlidCredentialsException("User not found"));
        return meetingRepository.findBySellerId(user.getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Meeting findMeeting(UUID id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found"));
    }

    private void asserSeller(Meeting meeting , String sellerEmail){
        if(!meeting.getSeller().getEmail().equals(sellerEmail)){
            throw new SecurityException("Only the seller can perform this action");
        }
    }



}