package com.thanhdeptrai.code.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class PaymentService {
    private final WebClient webClient =  WebClient.create("https://api.payment.mock");

    public String processPayment(String userId, double amount) {
        //stimulate payment process

        return "success";
    }

    public String processPaymentSuccess(String userId, double amount, boolean isSuccess) {
        //stimulate payment process
        if (isSuccess) {
            return processPayment(userId, amount);
        } else {
            return Strings.EMPTY;
        }
    }
}
