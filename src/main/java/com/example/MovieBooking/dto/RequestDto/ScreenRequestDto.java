package com.example.MovieBooking.dto.RequestDto;

import lombok.Data;

@Data
public class ScreenRequestDto {
    private String name;
    private String screenType;
    private Long theaterId; // The ID of the theater this screen belongs to
}