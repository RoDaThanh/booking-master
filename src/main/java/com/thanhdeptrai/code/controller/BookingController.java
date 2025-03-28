package com.thanhdeptrai.code.controller;

import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<Booking> bookSeat(
            @RequestParam Long seatId,
            @RequestParam String userId) {
        Booking res = bookingService.doBooking(seatId, userId);
        return ResponseEntity.accepted().body(res);
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<BookingStatus> getStatus(@PathVariable UUID id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking.getStatus());

    }

    @GetMapping("/booking")
    public ResponseEntity<Booking> getByPaymentID(@RequestParam String id) {
        Booking booking = bookingService.getBookingByPaymentIntentId(id);
        return ResponseEntity.ok(booking);

    }
}
