package com.example.MovieBooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponseDto {
    private Long id;
    private String seatNumber;
    private String seatType;
}
