package com.thanhdeptrai.code.controller;

import com.thanhdeptrai.code.dto.BookingRequest;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    // disable queue for now
    //private final RabbitTemplate rabbitTemplate;

    private final BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<Booking> bookSeat(
            @RequestParam Long seatId,
            @RequestParam String userId) {

//        String idempotencyKey = UUID.randomUUID().toString();
//         disable queue for now
//        rabbitTemplate.convertAndSend("bookingQueue", new BookingRequest(seatId, userId));
        Booking res = bookingService.doBooking(seatId, userId);

        return ResponseEntity.accepted().body(res);
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<BookingStatus> getStatus(@PathVariable UUID id) {
        try {
            Booking booking = bookingService.getBookingById(id);

            return ResponseEntity.ok(booking.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
