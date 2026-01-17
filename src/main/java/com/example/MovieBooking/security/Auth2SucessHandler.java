package com.example.MovieBooking.security;
import com.example.MovieBooking.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class Auth2SucessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public Auth2SucessHandler(@Lazy AuthService authService) {
        this.authService = authService;
    }
    @Value("${app.redirect.url:https://showtime-tix-frontend45.vercel.app}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. Cast the authentication to get OAuth2 token and user details
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. tells which OAuth2 provider was used it is a String value(Google, Facebook, etc.)
        String registrationId = token.getAuthorizedClientRegistrationId();

        // 3. Call service (Service handles DB check and generates Cookies)
        ResponseEntity<String> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User, registrationId);
        // now TRANSFER THE COOKIES from the ResponseEntity to the actual HttpServletResponse
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                response.addHeader(HttpHeaders.SET_COOKIE, cookie);
            }
        }

        // 5. SECURE REDIRECT: Redirect to React without tokens in the URL
        response.sendRedirect(redirectUrl);
    }
}