package com.thanhdeptrai.code.processor;

import com.thanhdeptrai.code.dto.BookingRequest;
import com.thanhdeptrai.code.exceptions.SeatNotFoundException;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.service.BookingService;
import com.thanhdeptrai.code.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingProcessor {
    private final BookingService bookingService;
    private final PaymentService paymentService;

    private static final Logger logger = LoggerFactory.getLogger(BookingProcessor.class);

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
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception exception) {
            logger.error("Server Error! Cannot booking now");
        }

    }
}
