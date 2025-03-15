package com.thanhdeptrai.code.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Seat seat;

    private String userId;

    private LocalDateTime reservedAt;

    private LocalDateTime expiresAt;

    private String paymentId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;
}
