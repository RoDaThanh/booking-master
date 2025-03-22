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
    private final PaymentService paymentService;

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Transactional
    public Booking reserveSeat(Long seatId, String userId) {
        String lockKey = "lockId:" + seatId + userId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));
        Booking booking = new Booking();
        if (locked != null && locked) {
            try {
                Seat reserveSeat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);

                if (reserveSeat.getStatus().equals(SeatStatus.AVAILABLE)) {
                    reserveSeat.setStatus(SeatStatus.RESERVED);
                    seatRepository.save(reserveSeat);

                    booking.setSeat(reserveSeat);
                    booking.setUserId(userId);
                    booking.setStatus(BookingStatus.INCOMPLETE);
                    booking.setReservedAt(LocalDateTime.now());
                    booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                    booking = bookingRepository.save(booking);
                } else {
                    throw new SeatNotAvailableException("Seat: " + reserveSeat.getSeatNumber()
                            + " is " + reserveSeat.getStatus());
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        return booking;
    }

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

    @Transactional
    public void confirmBooking(UUID bookingId, String paymentId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);

        booking.setPaymentId(paymentId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSeat().setStatus(SeatStatus.BOOKED);
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }

    @Transactional
    public void releaseSeat(UUID bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        booking.setStatus(BookingStatus.EXPIRED);
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }


    public Booking getBookingById(UUID id) throws Exception {
        return bookingRepository.findById(id).orElseThrow(BookingNotFoundException::new);
    }
}
