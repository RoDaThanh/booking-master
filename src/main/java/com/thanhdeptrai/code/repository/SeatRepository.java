package com.thanhdeptrai.code.repository;

import com.thanhdeptrai.code.model.Seat;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query("SELECT s FROM Seat s where s.event.id = :eventId")
    List<Seat> findByEventId(@Param("eventId")Long eventId);
}

