package com.swaplio.swaplio_backend.dto.meeting;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Sent by the SELLER to respond to a PENDING meeting request.
 *
 * action = CONFIRMED  → accept buyer's original time as-is
 * action = REJECTED   → reject the request entirely
 * action = RESCHEDULED → propose a new time (proposedDate + proposedTime required)
 */
public record SellerRespondRequest(

        @NotNull(message = "Action is required: CONFIRMED, REJECTED, or RESCHEDULED")
        MeetingStatus action,

        // required only when action = RESCHEDULED
        LocalDate proposedDate,
        LocalTime proposedTime,
        String proposedLocation,  // optional — falls back to original location if null

        String sellerNote         // optional note to buyer
) {}