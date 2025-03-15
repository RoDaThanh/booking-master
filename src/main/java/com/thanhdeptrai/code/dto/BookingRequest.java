package com.thanhdeptrai.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long seatId;
    private String userId;
}
