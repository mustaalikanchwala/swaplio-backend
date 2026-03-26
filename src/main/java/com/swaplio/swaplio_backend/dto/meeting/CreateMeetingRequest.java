package com.swaplio.swaplio_backend.dto.meeting;


import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateMeetingRequest(

        @NotNull
        UUID listingId,

        @NotNull
        LocalDate meetingDate,

        @NotNull
        LocalTime meetingTime,

        @NotBlank
        String location,

        String notes

) {}