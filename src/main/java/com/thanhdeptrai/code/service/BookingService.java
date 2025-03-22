package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.exceptions.BookingNotFoundException;
import com.thanhdeptrai.code.exceptions.SeatNotAvailableException;
import com.thanhdeptrai.code.exceptions.SeatNotFoundException;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.BookingRepository;
import com.thanhdeptrai.code.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public Booking doBooking(Long seatId, String userId) {
        String lockKey = "lockId:" + seatId;
        // try to acquire Redis lock for 30 seconds
        boolean isLocked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "isLocked", Duration.ofSeconds(30)));

        if (!isLocked) {
            throw new SeatNotAvailableException("Seat: " + seatId + " is currently being booked by another user. [redis lock]");
        }
        try {
            Seat reserveSeat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
            if (SeatStatus.AVAILABLE != reserveSeat.getStatus()) {
                throw new SeatNotAvailableException("Seat: " + seatId + " is currently being booked by another user.");
            }
            // update status and persist to db
            reserveSeat.setStatus(SeatStatus.RESERVED);
            seatRepository.save(reserveSeat);

            // create booking record
            return createBooking(reserveSeat, userId);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private Booking createBooking(Seat reserveSeat, String userId) {
        Booking booking = new Booking();
        booking.setSeat(reserveSeat);
        booking.setUserId(userId);
        booking.setStatus(BookingStatus.INCOMPLETE);
        booking.setReservedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(BookingNotFoundException::new);
    }
}
