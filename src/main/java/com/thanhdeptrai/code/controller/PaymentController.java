package com.thanhdeptrai.code.controller;

import com.stripe.exception.StripeException;
import com.thanhdeptrai.code.service.BookingService;
import com.thanhdeptrai.code.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam String paymentIntentId) {
        try {
            String paymentMessage = bookingService.confirmBooking(paymentIntentId);
            if ("Payment confirmed".equals(paymentMessage)) {
                return ResponseEntity.ok(paymentMessage);
            } else {
                return ResponseEntity.badRequest().body(paymentMessage);
            }
        } catch (StripeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
