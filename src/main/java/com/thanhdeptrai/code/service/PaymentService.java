package com.thanhdeptrai.code.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.SeatStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final BookingService bookingService;

    public String confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        paymentIntent.confirm();

        if ("succeeded".equals(paymentIntent.getStatus())) {
            Booking booking = bookingService.getBookingByPaymentIntentId(paymentIntentId);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.getSeat().setStatus(SeatStatus.BOOKED);
            bookingService.saveBooking(booking);
            return "Payment confirmed";
        } else {
            return "Payment failed";
        }
    }

    public PaymentIntent createPaymentIntent(double price, String usd) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Map<String, Object> params = new HashMap<>();
            params.put("amount", (long) (price * 100));
            params.put("currency", usd);
            params.put("payment_method_types", "card");
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

}
