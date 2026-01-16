package com.example.MovieBooking.dto;

import lombok.Data;
import java.time.LocalDateTime;

// @Data includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @NoArgsConstructor
@Data
public class ShowResponseDto {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Flattened data from the Movie entity
    private String movieTitle;
    private String moviePosterUrl;

    // Flattened data from the Screen and Theater entities
    private String screenName;
    private String theaterName;

    // Note: We intentionally do NOT include List<ShowSeatDto> here.
    // That data should be fetched from a separate endpoint like:
    // GET /api/shows/{showId}/seats
}