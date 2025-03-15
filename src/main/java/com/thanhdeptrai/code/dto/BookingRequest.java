package com.thanhdeptrai.code.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long seatId;
    private String userId;
}
