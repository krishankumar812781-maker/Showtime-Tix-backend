package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.ScreenRequestDto;
import com.example.MovieBooking.dto.ScreenResponseDto;
import com.example.MovieBooking.service.ScreenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    /**
     * Creates a new screen. (Admin Only)
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ScreenResponseDto> addScreen(@RequestBody ScreenRequestDto screenRequestDto) {
        ScreenResponseDto newScreen = screenService.addScreen(screenRequestDto);
        return new ResponseEntity<>(newScreen, HttpStatus.CREATED);
    }

    /**
     * Gets all screens for a specific theater. (Public)
     * This is an alternative to the /api/theaters/{id}/screens endpoint.
     */
    @GetMapping("/theater/{theaterId}") // ⚡ Added a path segment for clarity
    public ResponseEntity<List<ScreenResponseDto>> getScreensByTheater(
            @PathVariable Long theaterId) { // ⚡ Changed to @PathVariable

        List<ScreenResponseDto> screens = screenService.getScreensByTheater(theaterId);
        return ResponseEntity.ok(screens);
    }
}