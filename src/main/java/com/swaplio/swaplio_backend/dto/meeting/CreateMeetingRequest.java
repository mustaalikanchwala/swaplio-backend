package com.swaplio.swaplio_backend.dto.meeting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Sent by the BUYER to request a meeting for a listing.
 * This creates a PENDING meeting — it does NOT confirm one.
 */
public record CreateMeetingRequest(

        @NotNull(message = "Listing ID is required")
        UUID listingId,

        @NotNull(message = "Preferred date is required")
        LocalDate meetingDate,

        @NotNull(message = "Preferred time is required")
        LocalTime meetingTime,

        @NotBlank(message = "Location is required")
        String location,

        String notes   // optional
) {}