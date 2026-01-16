package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.SeatRequestDto;
import com.example.MovieBooking.dto.SeatResponseDto;
import com.example.MovieBooking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addSeats(@RequestBody SeatRequestDto requestDto) {
        return ResponseEntity.ok(seatService.addSeats(requestDto));
    }

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<List<SeatResponseDto>> getSeatsByScreen(@PathVariable Long screenId) {
        return ResponseEntity.ok(seatService.getSeatsByScreen(screenId));
    }

    @DeleteMapping("/screen/{screenId}")
    public ResponseEntity<String> deleteAllSeatsByScreen(@PathVariable Long screenId) {
        seatService.clearSeatsByScreen(screenId);
        return ResponseEntity.ok("All seats removed for this screen");
    }

}