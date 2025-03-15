package com.thanhdeptrai.code.repository;

import com.thanhdeptrai.code.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    // Custom query to find expired reservations
    @Query("SELECT b FROM Booking b WHERE b.expiresAt < :currentTime AND b.status = 'RESERVED'")
    List<Booking> findExpiredReservations(LocalDateTime currentTime);
}
