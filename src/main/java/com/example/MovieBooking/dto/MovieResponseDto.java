package com.example.MovieBooking.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponseDto {
    private Long id;
    private String title;
    private String plot;      // ⚡ Matches updated Entity
    private String rating;    // ⚡ Matches updated Entity
    private String language;
    private String genre;
    private String duration;  // ⚡ Matches OMDb "148 min"
    private String posterUrl;
    private String director;  // ⚡ New
    private String actors;    // ⚡ New
}