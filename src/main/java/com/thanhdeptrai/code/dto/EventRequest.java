package com.thanhdeptrai.code.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EventRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private double price;
    private int totalSeats;
}
