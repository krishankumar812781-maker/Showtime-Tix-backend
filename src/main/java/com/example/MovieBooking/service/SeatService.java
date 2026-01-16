package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.SeatRequestDto;
import com.example.MovieBooking.dto.SeatResponseDto; // ⚡ Ensure this import exists
import com.example.MovieBooking.entity.Screen;
import com.example.MovieBooking.entity.Seat;
import com.example.MovieBooking.repository.ScreenRepository;
import com.example.MovieBooking.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Transactional
    public String addSeats(SeatRequestDto requestDto) {
        // 1. Validate the Screen exists
        Screen screen = screenRepository.findById(requestDto.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        // 2. Map DTO list to Entity list
        List<Seat> seatsToSave = requestDto.getSeats().stream()
                .map(seatInfo -> {
                    Seat seat = new Seat();
                    seat.setScreen(screen);
                    seat.setSeatNumber(seatInfo.getSeatNumber());
                    seat.setSeatType(seatInfo.getSeatType());
                    return seat;
                })
                .toList();

        // 3. Perform bulk save for performance
        seatRepository.saveAll(seatsToSave);
        return "Added " + seatsToSave.size() + " seats to screen " + screen.getName();
    }

    /**
     * Fetches all seats for a specific screen and converts them to DTOs
     * for the AdminSeatManager preview panel.
     */
    public List<SeatResponseDto> getSeatsByScreen(Long screenId) {
        // 1. Fetch raw entities from database
        List<Seat> seats = seatRepository.findByScreenId(screenId);

        // 2. Transform Entities into Response DTOs
        return seats.stream().map(seat -> {
            SeatResponseDto dto = new SeatResponseDto();
            dto.setId(seat.getId());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setSeatType(seat.getSeatType());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void clearSeatsByScreen(Long screenId) {
        // Optional: Check if any of these seats have bookings before deleting
        seatRepository.deleteByScreenId(screenId);
    }
}