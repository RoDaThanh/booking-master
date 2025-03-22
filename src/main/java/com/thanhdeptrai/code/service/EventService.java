package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.dto.EventRequest;
import com.thanhdeptrai.code.model.Event;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final SeatService seatService;

    @Transactional
    public Event createEvent(EventRequest eventRequest) {
        Event event = Event.builder()
        .name(eventRequest.getName())
        .date(LocalDateTime.now())
        .price(eventRequest.getPrice())
        .totalSeats(eventRequest.getTotalSeats()).build();

        Event createdEvent = eventRepo.save(event);
        seatService.createSeats(eventRequest.getTotalSeats(), createdEvent);

        return createdEvent;
    }

    public List<Seat> getSeatsByEventId(Long eventId) {
        return seatService.getSeatsByEventId(eventId);
    }
}
