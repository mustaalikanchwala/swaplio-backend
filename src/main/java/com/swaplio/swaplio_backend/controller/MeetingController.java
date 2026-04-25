// controller/MeetingController.java
package com.swaplio.swaplio_backend.controller;

import com.swaplio.swaplio_backend.dto.meeting.BuyerRespondRequest;
import com.swaplio.swaplio_backend.dto.meeting.CreateMeetingRequest;
import com.swaplio.swaplio_backend.dto.meeting.MeetingResponse;
import com.swaplio.swaplio_backend.dto.meeting.SellerRespondRequest;
import com.swaplio.swaplio_backend.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    // ─────────────────────────────────────────────────────────────────────────
    // BUYER endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/meetings
     * Buyer sends a meeting request for a listing → status: PENDING
     */
    @Operation(summary = "Request a meeting (buyer)", description =
            "Buyer requests a meeting for a listing. Creates a PENDING request visible to the seller.")
    @PostMapping
    public ResponseEntity<MeetingResponse> requestMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.requestMeeting(request, auth.getName()));
    }

    /**
     * PATCH /api/meetings/{id}/buyer-respond
     * Buyer responds to a RESCHEDULED proposal from the seller.
     * Allowed actions: CONFIRMED (accept new time) | REJECTED (decline)
     */
    @Operation(summary = "Buyer responds to reschedule proposal", description =
            "After seller proposes a new time (RESCHEDULED), buyer can CONFIRM or REJECT it.")
    @PatchMapping("/{id}/buyer-respond")
    public ResponseEntity<MeetingResponse> buyerRespond(
            @PathVariable UUID id,
            @Valid @RequestBody BuyerRespondRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.buyerRespondToReschedule(id, request, auth.getName()));
    }

    /**
     * GET /api/meetings/my/buying
     * All meeting requests the logged-in user sent as a buyer.
     */
    @Operation(summary = "Get my meetings as buyer")
    @GetMapping("/my/buying")
    public ResponseEntity<List<MeetingResponse>> getMyMeetingsAsBuyer(
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.getMyMeetingsAsBuyer(auth.getName()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SELLER endpoints
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PATCH /api/meetings/{id}/seller-respond
     * Seller responds to a PENDING request.
     * Allowed actions: CONFIRMED | REJECTED | RESCHEDULED (requires proposedDate + proposedTime)
     */
    @Operation(summary = "Seller responds to a meeting request", description =
            "Seller can CONFIRM the buyer's time, REJECT the request, " +
                    "or RESCHEDULE by proposing a new date/time.")
    @PatchMapping("/{id}/seller-respond")
    public ResponseEntity<MeetingResponse> sellerRespond(
            @PathVariable UUID id,
            @Valid @RequestBody SellerRespondRequest request,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.sellerRespond(id, request, auth.getName()));
    }

    /**
     * GET /api/meetings/my/selling
     * All incoming meeting requests the logged-in user received as a seller.
     */
    @Operation(summary = "Get my meetings as seller")
    @GetMapping("/my/selling")
    public ResponseEntity<List<MeetingResponse>> getMyMeetingsAsSeller(
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.getMyMeetingsAsSeller(auth.getName()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SHARED endpoints (buyer or seller)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/meetings/{id}
     * Fetch a single meeting. Only the buyer or seller of that meeting can access it.
     */
    @Operation(summary = "Get a single meeting")
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(
            @PathVariable UUID id,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.getMeeting(id, auth.getName()));
    }

    /**
     * PATCH /api/meetings/{id}/cancel
     * Either party can cancel a CONFIRMED meeting.
     */
    @Operation(summary = "Cancel a confirmed meeting")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MeetingResponse> cancelMeeting(
            @PathVariable UUID id,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.cancelMeeting(id, auth.getName()));
    }

    /**
     * PATCH /api/meetings/{id}/complete
     * Either party marks a CONFIRMED meeting as completed (item exchanged).
     */
    @Operation(summary = "Mark meeting as completed")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<MeetingResponse> completeMeeting(
            @PathVariable UUID id,
            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.completeMeeting(id, auth.getName()));
    }

}