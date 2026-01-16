package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.TheaterRequestDto;
import com.example.MovieBooking.dto.ShowResponseDto;
import com.example.MovieBooking.dto.TheaterResponseDto;
import com.example.MovieBooking.dto.TheaterResponseDto;
import com.example.MovieBooking.service.ShowService;
import com.example.MovieBooking.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ShowService showService;

    // --- Public Endpoints (for any user) ---

    @GetMapping
    public ResponseEntity<?> getAllTheaters() {
        List<TheaterResponseDto> theaters = theaterService.getAllTheaters();
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }
    @GetMapping("/city")
    public ResponseEntity<?> getTheaterByLocation(@RequestParam String city) {
        List<TheaterResponseDto> theaters = theaterService.findTheatersByCity(city);
        return new ResponseEntity<>(theaters, HttpStatus.OK);
    }

    @GetMapping("/{theaterId}/shows")
    public ResponseEntity<List<ShowResponseDto>> getShowsForTheater(@PathVariable Long theaterId) {
        List<ShowResponseDto> shows = showService.getShowsByTheaterId(theaterId); //its inside show service
        return ResponseEntity.ok(shows);
    }

    // --- Admin Endpoints (Secured) ---

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addTheater(@RequestBody TheaterRequestDto theaterDTO) {
        TheaterResponseDto newTheater = theaterService.addTheater(theaterDTO);
        return new ResponseEntity<>(newTheater, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateTheater(@PathVariable Long id, @RequestBody TheaterRequestDto theaterDTO) {
        TheaterResponseDto updatedTheater = theaterService.updateTheater(id, theaterDTO);
        return new ResponseEntity<>(updatedTheater, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return new ResponseEntity<>("Theater deleted successfully", HttpStatus.OK);
    }
}
