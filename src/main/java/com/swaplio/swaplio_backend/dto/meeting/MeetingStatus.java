package com.swaplio.swaplio_backend.dto.meeting;

public enum MeetingStatus {
    PENDING,        // buyer sent request, waiting for seller
    CONFIRMED,      // seller accepted (original or proposed time)
    REJECTED,       // seller rejected / buyer rejected reschedule
    RESCHEDULED,    // seller proposed a new time, waiting for buyer
    COMPLETED,      // item exchanged
    CANCELLED       // cancelled after confirmation
}