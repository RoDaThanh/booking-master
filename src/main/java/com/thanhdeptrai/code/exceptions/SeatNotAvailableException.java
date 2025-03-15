package com.thanhdeptrai.code.exceptions;

public class SeatNotAvailableException extends RuntimeException {
    public SeatNotAvailableException() {
        super("Seat is no longer available");
    }
}
