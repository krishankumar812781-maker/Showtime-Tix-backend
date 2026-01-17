package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.JwtAuthResponseDto;
import com.example.MovieBooking.dto.UserResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.xml.transform.sax.SAXResult;

public interface AuthService {
    ResponseEntity<String> login(LoginDto loginDto);
    String register(RegisterDto registerDto);

    ResponseEntity<String> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

    ResponseEntity<String> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId);

    UserResponseDto getCurrentUser();

    ResponseEntity<String> logout();

}