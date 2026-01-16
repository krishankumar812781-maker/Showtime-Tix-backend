package com.example.MovieBooking.dto.RequestDto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class ShowUpdateRequestDto {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // We make this optional. If the admin doesn't send this map,
    // we won't update any prices.
    private Map<String, BigDecimal> seatPrices=new HashMap<>();
}
