// controller/MeetingController.java
package com.swaplio.swaplio_backend.controller;

import com.swaplio.swaplio_backend.dto.meeting.CreateMeetingRequest;
import com.swaplio.swaplio_backend.model.Meeting;
import com.swaplio.swaplio_backend.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    public ResponseEntity<Meeting> scheduleMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            Authentication auth) {
        return ResponseEntity.ok(meetingService.scheduleMeeting(request, auth.getName()));
    }

    @GetMapping("/my/buying")
    public ResponseEntity<List<Meeting>> getMyBuyingMeetings(Authentication auth) {
        return ResponseEntity.ok(meetingService.getMyMeetingsAsBuyer(auth.getName()));
    }

    @GetMapping("/my/selling")
    public ResponseEntity<List<Meeting>> getMySellingMeetings(Authentication auth) {
        return ResponseEntity.ok(meetingService.getMyMeetingsAsSeller(auth.getName()));
    }
}