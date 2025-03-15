package com.thanhdeptrai.code.scheduler;

import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.BookingRepository;
import com.thanhdeptrai.code.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ExpirationScheduler {
    private final BookingRepository bookingRepo;
    private final SeatRepository seatRepo;

    @Scheduled(fixedRate = 60000)
    public void releaseExpiredReservation() {
        bookingRepo.findExpiredReservations(LocalDateTime.now())
                .forEach(booking -> {
                    booking.getSeat().setStatus(SeatStatus.AVAILABLE);
                    booking.setStatus(BookingStatus.EXPIRED);
                    seatRepo.save(booking.getSeat());
                    bookingRepo.save(booking);
                });
    }
}
