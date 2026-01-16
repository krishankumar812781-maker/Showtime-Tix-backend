package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.ShowRequestDto;
import com.example.MovieBooking.dto.ShowResponseDto;
import com.example.MovieBooking.dto.RequestDto.ShowUpdateRequestDto;
import com.example.MovieBooking.dto.ShowSeatDto;
import com.example.MovieBooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    /**
     * Creates a new show (Admin Only)
     * POST /api/shows
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ShowResponseDto> createShow(@RequestBody ShowRequestDto showRequestDto){
        return new ResponseEntity<>(showService.createShow(showRequestDto), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ShowResponseDto>> getAllShows() {
        return ResponseEntity.ok(showService.getAllShows());
    }

    @GetMapping
    public ResponseEntity<List<ShowResponseDto>> getShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false)  LocalDate date
    ){
        return ResponseEntity.ok(showService.getFilteredShows(movieId, city, theaterId, date));
    }

    /**
     * Gets details for a single specific show.
     * GET /api/shows/1
     */
    @GetMapping("/{showId}")
    public ResponseEntity<ShowResponseDto> getShowById(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getShowById(showId));
    }

    /**
     * Gets the seat layout for a specific show.
     * GET /api/shows/1/seats
     */
    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatDto>> getShowSeatLayout(@PathVariable Long showId) {
        return ResponseEntity.ok(showService.getSeatsForShow(showId));
    }

    /**
     * Updates an existing show (Admin Only)
     */
    @PutMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable Long showId,
            @RequestBody ShowUpdateRequestDto showUpdateRequestDto){
        return ResponseEntity.ok(showService.updateShow(showId, showUpdateRequestDto));
    }

    /**
     * Deletes a show (Admin Only)
     */
    @DeleteMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long showId){
        showService.deleteShow(showId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}