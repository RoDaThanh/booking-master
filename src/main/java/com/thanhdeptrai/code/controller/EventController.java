package com.thanhdeptrai.code.controller;

import com.thanhdeptrai.code.dto.EventRequest;
import com.thanhdeptrai.code.model.Event;
import com.thanhdeptrai.code.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/event")
    public ResponseEntity<Event> createEvent(@RequestBody EventRequest eventRequest) {
        Event event = eventService.createEvent(eventRequest);
        return ResponseEntity.ok(event);
    }

    // get seats of event by id
    @GetMapping("/event/{id}/seats")
    public ResponseEntity<?> getSeatsByEventId(@PathVariable Long id) {
        // get seats by event id

        return ResponseEntity.ok(eventService.getSeatsByEventId(id));
    }



}
