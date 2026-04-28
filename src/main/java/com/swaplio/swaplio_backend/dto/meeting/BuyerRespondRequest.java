package com.swaplio.swaplio_backend.dto.meeting;

import jakarta.validation.constraints.NotNull;

/**
 * Sent by the BUYER when a seller has proposed a new time (RESCHEDULED status).
 *
 * action = CONFIRMED → buyer accepts the seller's proposed time
 * action = REJECTED  → buyer rejects, meeting is closed
 */
public record BuyerRespondRequest(

        @NotNull(message = "Action is required: CONFIRMED or REJECTED")
        MeetingStatus action
) {}