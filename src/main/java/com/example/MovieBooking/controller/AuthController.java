package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.UserResponseDto;
import com.example.MovieBooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto) {
        // Correct: Pass through the ResponseEntity containing cookies
        return authService.login(loginDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return authService.logout();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        // The Filter has already verified the cookie and set the SecurityContext
        UserResponseDto userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(userResponse);
    }

    // --- FIXED REFRESH ENDPOINT ---
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        // ⚡ FIX: Service now returns ResponseEntity<String> with new cookies
        return authService.refreshToken(refreshTokenRequestDto);
    }

}