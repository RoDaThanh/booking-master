package com.thanhdeptrai.code.exceptions;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException() {
        super("Seat not found");
    }
}
