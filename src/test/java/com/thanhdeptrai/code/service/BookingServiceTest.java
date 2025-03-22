package com.thanhdeptrai.code.service;

import com.thanhdeptrai.code.exceptions.SeatNotAvailableException;
import com.thanhdeptrai.code.exceptions.SeatNotFoundException;
import com.thanhdeptrai.code.model.Booking;
import com.thanhdeptrai.code.model.BookingStatus;
import com.thanhdeptrai.code.model.Seat;
import com.thanhdeptrai.code.model.SeatStatus;
import com.thanhdeptrai.code.repository.BookingRepository;
import com.thanhdeptrai.code.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;

    private final Long SEAT_ID = 1L;
    private final String USER_1 = "user1";

    @BeforeEach
    void setUp() {
        // Mocking the behavior of RedisTemplate
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @Transactional
    void doBooking_SuccessfulBooking() {
        // setup
        String lockKey = "lockId:" + SEAT_ID;
        Seat seat = new Seat();
        seat.setId(SEAT_ID);
        seat.setStatus(SeatStatus.AVAILABLE);

        when(valueOperations.setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)))).thenReturn(true);
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenReturn(seat);

        // trigger
        bookingService.doBooking(SEAT_ID, USER_1);

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking capturedBooking = bookingCaptor.getValue();

        // assert
        assertNotNull(capturedBooking);
        assertEquals(SEAT_ID, capturedBooking.getSeat().getId());
        assertEquals(USER_1, capturedBooking.getUserId());
        assertEquals(SeatStatus.RESERVED, capturedBooking.getSeat().getStatus());
        assertEquals(BookingStatus.INCOMPLETE, capturedBooking.getStatus());
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)));
        verify(seatRepository).findById(SEAT_ID);
        verify(seatRepository).save(seat);
        verify(bookingRepository).save(any(Booking.class));
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @Transactional
    void doBooking_SeatNotAvailable_LockNotAcquired() {
        // setup
        String lockKey = "lockId:" + SEAT_ID;
        when(valueOperations.setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)))).thenReturn(false);

        // trigger & assert
        SeatNotAvailableException exception = assertThrows(SeatNotAvailableException.class, () -> {
            bookingService.doBooking(SEAT_ID, USER_1);
        });

        // assert
        assertEquals("Seat: " + SEAT_ID + " is currently being booked by another user. [redis lock]", exception.getMessage());
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)));
        verify(seatRepository, never()).findById(anyLong());
        verify(seatRepository, never()).save(any(Seat.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @Transactional
    void doBooking_SeatNotAvailable_SeatAlreadyReserved() {
        // setup
        String lockKey = "lockId:" + SEAT_ID;
        Seat seat = new Seat();
        seat.setId(SEAT_ID);
        seat.setStatus(SeatStatus.RESERVED);

        when(valueOperations.setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)))).thenReturn(true);
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

        // trigger & Assert
        SeatNotAvailableException exception = assertThrows(SeatNotAvailableException.class, () -> {
            bookingService.doBooking(SEAT_ID, USER_1);
        });

        // Assert
        assertEquals("Seat: " + SEAT_ID + " is currently being booked by another user.", exception.getMessage());
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)));
        verify(seatRepository).findById(SEAT_ID);
        verify(seatRepository, never()).save(any(Seat.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(redisTemplate).delete(lockKey);
    }

    @Test
    @Transactional
    void doBooking_SeatNotFound() {
        // setup
        String lockKey = "lockId:" + SEAT_ID;
        when(valueOperations.setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)))).thenReturn(true);
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

        // trigger  & Assert
        SeatNotFoundException exception = assertThrows(SeatNotFoundException.class, () -> {
            bookingService.doBooking(SEAT_ID, USER_1);
        });

        // Assert
        assertNotNull(exception);
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("isLocked"), eq(Duration.ofSeconds(30)));
        verify(seatRepository).findById(SEAT_ID);
        verify(seatRepository, never()).save(any(Seat.class));
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(redisTemplate).delete(lockKey);
    }
}
