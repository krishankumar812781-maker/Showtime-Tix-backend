package com.example.MovieBooking.security;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                // ⚡ Must be true for Cross-Site (Vercel -> Render)
                .secure(true)
                .path("/")
                .maxAge(3600) // 1 hour
                // ⚡ Required for cross-domain cookies
                .sameSite("None")
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                // ⚡ Must be true for Cross-Site (Vercel -> Render)
                .secure(true)
                .path("/")
                .maxAge(604800) // 7 days
                // ⚡ Required for cross-domain cookies
                .sameSite("None")
                .build();
    }

    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("None")
                .build();
    }
}