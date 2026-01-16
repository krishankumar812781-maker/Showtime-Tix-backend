package com.example.MovieBooking.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShowRequestDto {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private long movieId;

    private long screenId;

    // Key: SeatType (String, e.g., "REGULAR", "PREMIUM")
    // Value: Price for that seat type
    private Map<String, BigDecimal> seatPrices=new HashMap<>();
}
