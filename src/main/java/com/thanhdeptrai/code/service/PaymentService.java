package com.thanhdeptrai.code.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
public class PaymentService {
    private final WebClient webClient =  WebClient.create("https://api.payment.mock");

    public String processPayment(String userId, double amount) {
        //stimulate payment process
//
        return null;
    }
}
