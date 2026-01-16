package com.example.MovieBooking.dto;

import lombok.Data;

@Data
public class ScreenResponseDto {
    private Long id;
    private String name;
    private String screenType;
    private String theaterName; // Flattened for convenience
}