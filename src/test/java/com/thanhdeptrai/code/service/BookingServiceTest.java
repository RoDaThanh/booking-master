package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.exceptions.SeatNotAvailableException;
import com.thanhdeptrai.code.exceptions.SeatNotFoundException;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.Event;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.BookingRepository;
import com.thanhdeptrai.code.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        // Mocking the behavior of RedisTemplate
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void reserveSeat_success() {
        // given
        Seat seat = new Seat(1L, "seat1", SeatStatus.AVAILABLE, 0L, new Event());
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(true);

        // when
       bookingService.reserveSeat(seat.getId(), "thanhdeptraiUser");

        // then
        assertEquals(seat.getStatus(), SeatStatus.RESERVED);
        verify(bookingRepository).save(any(Booking.class));
        verify(seatRepository).save(seat);
    }

    @Test
    void reserveSeat_seatNotFound() {
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(true);
        assertThrows(SeatNotFoundException.class, () ->
                bookingService.reserveSeat(1L, "thanhdeptraiUser"));
    }

    @Test
    void reserveSeat_SeatNotAvailable() {
        // given
        Seat seat = new Seat(1L, "seat1", SeatStatus.RESERVED, 0L, new Event());
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(true);
        // then
        assertThrows(SeatNotAvailableException.class, () ->
                bookingService.reserveSeat(1L, "thanhdeptraiUser"));
    }
}
