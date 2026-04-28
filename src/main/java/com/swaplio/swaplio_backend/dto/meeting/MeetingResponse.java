package com.swaplio.swaplio_backend.dto.meeting;

import com.swaplio.swaplio_backend.model.Meeting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Returned by every meeting endpoint.
 * Both buyer and seller see the same shape — the frontend decides
 * what to render based on the status and their role.
 */
public record MeetingResponse(

        UUID id,

        // listing summary
        UUID listingId,
        String listingTitle,

        // parties
        UUID buyerId,
        String buyerName,
        UUID sellerId,
        String sellerName,

        // buyer's originally requested time
        LocalDate meetingDate,
        LocalTime meetingTime,
        String location,
        String notes,

        // seller's proposed alternate time (non-null only when RESCHEDULED)
        LocalDate proposedDate,
        LocalTime proposedTime,
        String proposedLocation,
        String sellerNote,

        MeetingStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Build a MeetingResponse from a Meeting entity. */
    public static MeetingResponse from(Meeting m) {
        return new MeetingResponse(
                m.getId(),
                m.getListing().getId(),
                m.getListing().getTitle(),
                m.getBuyer().getId(),
                m.getBuyer().getFullName(),
                m.getSeller().getId(),
                m.getSeller().getFullName(),
                m.getMeetingDate(),
                m.getMeetingTime(),
                m.getLocation(),
                m.getNotes(),
                m.getProposedDate(),
                m.getProposedTime(),
                m.getProposedLocation(),
                m.getSellerNote(),
                m.getStatus(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}