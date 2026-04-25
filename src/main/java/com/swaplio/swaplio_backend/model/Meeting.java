package com.swaplio.swaplio_backend.model;


import com.swaplio.swaplio_backend.dto.meeting.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // ── Parties ──────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // ── Buyer's originally requested time ────────────────────────────────────
    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(nullable = false)
    private LocalTime meetingTime;

    @Column(nullable = false)
    private String location;

    private String notes;

    // ── Seller's proposed alternate time (only set when RESCHEDULED) ─────────
    private LocalDate proposedDate;
    private LocalTime proposedTime;
    private String proposedLocation;
    private String sellerNote;         // optional note when accepting / rescheduling

    // ── Status ───────────────────────────────────────────────────────────────
    /**
     * Lifecycle:
     *
     *  PENDING
     *    ├─► CONFIRMED    (seller accepts buyer's original time)
     *    ├─► REJECTED     (seller rejects entirely)
     *    └─► RESCHEDULED  (seller proposes a new time)
     *              ├─► CONFIRMED   (buyer accepts seller's proposed time)
     *              └─► REJECTED    (buyer rejects seller's proposed time)
     *
     *  CONFIRMED ──► COMPLETED  (either party marks done)
     *  CONFIRMED ──► CANCELLED  (either party cancels)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PENDING;

    // ── Timestamps ───────────────────────────────────────────────────────────
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}