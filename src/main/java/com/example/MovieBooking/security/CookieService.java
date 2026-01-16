package com.example.MovieBooking.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false) // Set to true in Production with HTTPS
                .path("/") // har api call ma cookie request ma a jaye
                .maxAge(3600) // 1 hour
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(604800) // 7 days
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, null)
                .httpOnly(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .build();
    }
}
