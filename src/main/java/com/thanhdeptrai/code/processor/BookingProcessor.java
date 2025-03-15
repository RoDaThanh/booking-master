package com.thanhdeptrai.code.processor;

import com.thanhdeptrai.code.dto.BookingRequest;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.service.BookingService;
import com.thanhdeptrai.code.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingProcessor {
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @RabbitListener(queues = "bookingQueue")
    public void processBooking(BookingRequest bookingRequest) {
        try {
            Booking booking = bookingService.reserveSeat(bookingRequest.getSeatId(), bookingRequest.getUserId());
            String paymentId = paymentService.processPayment(bookingRequest.getUserId(),
                    booking.getSeat().getEvent().getPrice());
            if (paymentId != null) {
                bookingService.confirmBooking(booking.getId(), paymentId);
            } else {
                bookingService.releaseSeat(booking.getId());
            }
        } catch (Exception e) {
            throw new RuntimeException("cannot booking now");
        }

    }
}
