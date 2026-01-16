package com.example.MovieBooking.dto.RequestDto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
public class BookingRequestDto {
    private Long showId;
    private List<Long> showSeatIds=new ArrayList<>();
}
