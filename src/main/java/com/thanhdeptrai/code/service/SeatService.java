package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.model.Event;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepo;

    public void createSeats(int totalSeats, Event event) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i <= totalSeats; i++) {
            String seatName = "A " + event.getName() + i;
            seats.add(Seat.builder()
                    .seatNumber(seatName)
                    .status(SeatStatus.AVAILABLE)
                    .event(event)
                    .build());
        }

        seatRepo.saveAll(seats);
    }
}
