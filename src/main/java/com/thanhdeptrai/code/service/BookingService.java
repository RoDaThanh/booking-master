package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.BookingRepository;
import com.thanhdeptrai.code.repository.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
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
    //TODO handle exception
    public Booking reserveSeat(Long seatId, String userId) throws Exception {
        String lockKey = "seat:" + seatId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));
        Booking booking = new Booking();
        if (locked != null && locked) {
            try {
                Seat reserveSeat = seatRepository.findById(seatId).orElseThrow(ChangeSetPersister.NotFoundException::new);

                if (reserveSeat.getStatus().equals(SeatStatus.AVAILABLE)) {
                    reserveSeat.setStatus(SeatStatus.RESERVED);
                    seatRepository.save(reserveSeat);


                    booking.setSeat(reserveSeat);
                    booking.setUserId(userId);
                    booking.setStatus(BookingStatus.RESERVED);
                    booking.setReservedAt(LocalDateTime.now());
                    booking.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                    booking = bookingRepository.save(booking);
                }
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        return booking;
    }

    @Transactional
    public void confirmBooking(UUID bookingId, String paymentId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(Exception::new);

        booking.setPaymentId(paymentId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSeat().setStatus(SeatStatus.BOOKED);
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }

    @Transactional
    public void releaseSeat(UUID bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(Exception::new);
        booking.getSeat().setStatus(SeatStatus.AVAILABLE);
        booking.setStatus(BookingStatus.EXPIRED);
        seatRepository.save(booking.getSeat());
        bookingRepository.save(booking);
    }


    public Booking getBookingById(UUID id) throws Exception {
        return bookingRepository.findById(id).orElseThrow(Exception::new);
    }
}
