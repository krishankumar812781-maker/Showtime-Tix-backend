package com.example.MovieBooking.dto;

import lombok.Data;

@Data
public class JwtAuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public JwtAuthResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}