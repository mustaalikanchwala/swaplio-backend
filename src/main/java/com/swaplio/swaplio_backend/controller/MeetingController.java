// controller/MeetingController.java
package com.swaplio.swaplio_backend.controller;

import com.swaplio.swaplio_backend.dto.meeting.*;
import com.swaplio.swaplio_backend.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
     *
     * All params are optional — send only what you need:
     *
     *   GET /api/meetings/my/buying                              → all meetings
     *   GET /api/meetings/my/buying?status=PENDING              → only pending
     *   GET /api/meetings/my/buying?status=RESCHEDULED          → needs buyer action
     *   GET /api/meetings/my/buying?startDate=2026-04-01&endDate=2026-04-30  → this month
     *   GET /api/meetings/my/buying?status=CONFIRMED&startDate=2026-04-01    → confirmed this month+
     */
    @Operation(
            summary = "Get my meetings as buyer",
            description = """
            Returns buyer's meetings with optional filters.
            All params are optional — omit any to skip that filter.
            
            status values: PENDING, RESCHEDULED, CONFIRMED, REJECTED, COMPLETED, CANCELLED
            dates format:  yyyy-MM-dd
            """
    )
    @GetMapping("/my/buying")
    public ResponseEntity<List<MeetingResponse>> getMyMeetingsAsBuyer(
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) MeetingStatus status,

            @Parameter(description = "Meetings on or after this date (optional) — format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Meetings on or before this date (optional) — format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.getMyMeetingsAsBuyer(
                        auth.getName(), status, startDate, endDate));
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

    // ─────────────────────────────────────────────────────────────────────────
    // SELLER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/meetings/my/selling
     *
     * All params are optional — send only what you need:
     *
     *   GET /api/meetings/my/selling                             → all meetings
     *   GET /api/meetings/my/selling?status=PENDING             → new requests needing action
     *   GET /api/meetings/my/selling?status=CONFIRMED           → upcoming confirmed meetings
     *   GET /api/meetings/my/selling?startDate=2026-04-01&endDate=2026-04-30  → this month
     *   GET /api/meetings/my/selling?status=PENDING&startDate=2026-04-01      → pending this month+
     */
    @Operation(
            summary = "Get my meetings as seller",
            description = """
            Returns seller's meetings with optional filters.
            All params are optional — omit any to skip that filter.
            
            status values: PENDING, RESCHEDULED, CONFIRMED, REJECTED, COMPLETED, CANCELLED
            dates format:  yyyy-MM-dd
            """
    )
    @GetMapping("/my/selling")
    public ResponseEntity<List<MeetingResponse>> getMyMeetingsAsSeller(
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) MeetingStatus status,

            @Parameter(description = "Meetings on or after this date (optional) — format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Meetings on or before this date (optional) — format: yyyy-MM-dd")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            Authentication auth) {

        return ResponseEntity.ok(
                meetingService.getMyMeetingsAsSeller(
                        auth.getName(), status, startDate, endDate));
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