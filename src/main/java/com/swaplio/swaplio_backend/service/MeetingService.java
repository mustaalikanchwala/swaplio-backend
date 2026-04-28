package com.swaplio.swaplio_backend.service;

// service/MeetingService.java
import com.swaplio.swaplio_backend.dto.meeting.*;
import com.swaplio.swaplio_backend.exception.ListingNotFoundException;
import com.swaplio.swaplio_backend.exception.MeetingNotFoundException;
import com.swaplio.swaplio_backend.model.Listing;
import com.swaplio.swaplio_backend.model.Meeting;
import com.swaplio.swaplio_backend.model.User;
import com.swaplio.swaplio_backend.repository.ListingRepository;
import com.swaplio.swaplio_backend.repository.MeetingRepository;
import com.swaplio.swaplio_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        assertSeller(meeting,sellerEmail);

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

    // ─────────────────────────────────────────────────────────────────────────
    // BUYER: Respond to a RESCHEDULED proposal
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * After the seller proposes a new time (RESCHEDULED), the buyer can:
     *   CONFIRMED → accept the seller's proposed time
     *              (the confirmed meeting now uses proposedDate/Time)
     *   REJECTED  → reject the seller's proposed time (meeting is closed)
     */
    @Transactional
    public MeetingResponse buyerRespondToReschedule(UUID meetingId,
                                                    BuyerRespondRequest request,
                                                    String buyerEmail) {

        Meeting meeting = findMeeting(meetingId);
        assertBuyer(meeting, buyerEmail);

        if (meeting.getStatus() != MeetingStatus.RESCHEDULED) {
            throw new IllegalStateException(
                    "This meeting is not awaiting your response. Current status: "
                            + meeting.getStatus());
        }

        switch (request.action()) {

            case CONFIRMED -> {
                // Promote the seller's proposed time to the confirmed time
                meeting.setMeetingDate(meeting.getProposedDate());
                meeting.setMeetingTime(meeting.getProposedTime());
                meeting.setLocation(meeting.getProposedLocation());
                // Clear proposed fields — they're now the main time
                meeting.setProposedDate(null);
                meeting.setProposedTime(null);
                meeting.setProposedLocation(null);
                meeting.setStatus(MeetingStatus.CONFIRMED);
            }

            case REJECTED -> meeting.setStatus(MeetingStatus.REJECTED);

            default -> throw new IllegalArgumentException(
                    "Invalid action. Allowed: CONFIRMED or REJECTED");
        }

        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EITHER PARTY: Cancel a confirmed meeting
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public MeetingResponse cancelMeeting(UUID meetingId, String userEmail) {

        Meeting meeting = findMeeting(meetingId);
        assertBuyerOrSeller(meeting, userEmail);

        if (meeting.getStatus() != MeetingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED meetings can be cancelled. Current status: "
                            + meeting.getStatus());
        }

        meeting.setStatus(MeetingStatus.CANCELLED);
        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EITHER PARTY: Mark meeting as completed
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public MeetingResponse completeMeeting(UUID meetingId, String userEmail) {

        Meeting meeting = findMeeting(meetingId);
        assertBuyerOrSeller(meeting, userEmail);

        if (meeting.getStatus() != MeetingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED meetings can be marked completed. Current status: "
                            + meeting.getStatus());
        }

        meeting.setStatus(MeetingStatus.COMPLETED);
        return MeetingResponse.from(meetingRepository.save(meeting));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ — Buyer's meetings (all optional filters)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param status    null = all statuses
     * @param startDate null = no lower bound
     * @param endDate   null = no upper bound
     */

    /** All meeting requests the logged-in user sent as a buyer. */
    public List<MeetingResponse> getMyMeetingsAsBuyer(String buyerEmail, MeetingStatus status, LocalDate startDate,LocalDate endDat) {
        User buyer = findUserByEmail(buyerEmail);
        return meetingRepository.findByBuyerFiltered(buyer,status,startDate,endDat)
                .stream()
                .map(MeetingResponse::from)
                .toList();
    }

    /** All incoming meeting requests the logged-in user received as a seller. */
    public List<MeetingResponse> getMyMeetingsAsSeller(String sellerEmail,MeetingStatus status,LocalDate startDate,LocalDate endDate) {
        User seller = findUserByEmail(sellerEmail);
        return meetingRepository.findBySellerFiltered(seller,status,startDate,endDate)
                .stream()
                .map(MeetingResponse::from)
                .toList();
    }

    /** Single meeting — accessible by buyer or seller only. */
    public MeetingResponse getMeeting(UUID meetingId, String userEmail) {
        Meeting meeting = findMeeting(meetingId);
        assertBuyerOrSeller(meeting, userEmail);
        return MeetingResponse.from(meeting);
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

    private void assertSeller(Meeting meeting , String sellerEmail){
        if(!meeting.getSeller().getEmail().equals(sellerEmail)){
            throw new SecurityException("Only the seller can perform this action");
        }
    }

    private void assertBuyer(Meeting meeting, String email) {
        if(!meeting.getBuyer().getEmail().equals(email)){
            throw new SecurityException("Only the buyer can perform this action");
        }
    }

    private void assertBuyerOrSeller(Meeting meeting, String email) {
        boolean isBuyer  = meeting.getBuyer().getEmail().equals(email);
        boolean isSeller = meeting.getSeller().getEmail().equals(email);
        if (!isBuyer && !isSeller) {
            throw new SecurityException("You are not part of this meeting");
        }
    }


}