package com.example.MovieBooking.dto.RequestDto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieRequestDto {
    private String title;     // Admin only needs to provide title to trigger OMDb
    private String language;
    // You can add other fields if you want to allow manual overrides
}