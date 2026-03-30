package com.swaplio.swaplio_backend.model;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "listing_images")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false)
    private String imageKey;

    @Builder.Default
    private boolean isPrimary = false;

    private int displayOrder;
}