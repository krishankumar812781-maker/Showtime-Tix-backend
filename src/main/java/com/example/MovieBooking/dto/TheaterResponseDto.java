package com.example.MovieBooking.dto;

import lombok.Data;

@Data
public class TheaterResponseDto {
    private Long id;
    private String name;
    private String address;
    private String city;
}