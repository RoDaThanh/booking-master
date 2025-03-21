package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.exceptions.BookingNotFoundException;
import com.thanhdeptrai.code.exceptions.SeatNotAvailableException;
import com.thanhdeptrai.code.exceptions.SeatNotFoundException;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.processor.BookingProcessor;
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
                    booking.setStatus(BookingStatus.RESERVED);
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

    public Booking doBooking(Long seatId, String userId) {
        Booking booking = null;
        try {
            booking = reserveSeat(seatId, userId);

            // check booking is null or not
            if (booking.getId() == null) {
                return booking;
            } else {
                String paymentId = paymentService.processPaymentSuccess(userId,
                        booking.getSeat().getEvent().getPrice(), true);
                if (paymentId != null) {
                    confirmBooking(booking.getId(), paymentId);
                } else {
                    releaseSeat(booking.getId());
                }
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception exception) {
            logger.error("Server Error! Cannot booking now");
        }
        return booking;
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
