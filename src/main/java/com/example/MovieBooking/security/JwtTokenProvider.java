package com.example.MovieBooking.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;


@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtAccessExpirationMs; //acess token expiration

    @Value("${app.jwt-refresh-expiration-milliseconds}")
    private long jwtRefreshExpirationMs; //refresh token expiration

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        return generateToken(username, authentication.getAuthorities(), jwtAccessExpirationMs);
    }

    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        return generateToken(username, authentication.getAuthorities(), jwtRefreshExpirationMs);
    }

    // --- Helper that includes the 'role' claim as fetched from DB ---
    private String generateToken(String username, Collection<? extends GrantedAuthority> authorities, long expirationMs) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationMs);

        // Convert Collection of Authorities to a single String
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(username)
                .claim("role", roles) // ⚡ HIGHLIGHT: This adds the roles to your JWT payload
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }



    // --- for OAuth2 : Methods to generate tokens with manual role injection "ROLE_USER" ---
    public String generateAccessTokenFromEmail(String email, String role) {
        return generateTokenWithManualRole(email, role, jwtAccessExpirationMs);
    }

    public String generateRefreshTokenFromEmail(String email, String role) {
        return generateTokenWithManualRole(email, role, jwtRefreshExpirationMs);
    }

    // --- Helper for manual role injection "ROLE_USER" passed (OAuth2 flows)
    private String generateTokenWithManualRole(String username, String role, long expirationMs) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }


    public Date getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parse(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}