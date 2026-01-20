package com.example.MovieBooking.security;

import com.example.MovieBooking.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class Auth2SucessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    public Auth2SucessHandler(@Lazy AuthService authService) {
        this.authService = authService;
    }

    // ⚡ Added a default value to prevent NullPointerException if property is missing
    @Value("${app.redirect.url:https://showtime-tix-frontend45.vercel.app/oauth2/callback}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        try {
            // 1. Cast the authentication to get OAuth2 token and user details
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 2. Determine provider (Google, GitHub, etc.)
            String registrationId = token.getAuthorizedClientRegistrationId();

            // 3. Call service (Service handles DB check and generates Cookies)
            // ⚡ This is where the 500 error usually happens if DB constraints are hit
            ResponseEntity<String> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User, registrationId);

            // 4. TRANSFER THE COOKIES from the ResponseEntity to the actual HttpServletResponse
            if (loginResponse.getStatusCode().is2xxSuccessful()) {
                List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
                if (cookies != null) {
                    for (String cookie : cookies) {
                        response.addHeader(HttpHeaders.SET_COOKIE, cookie);
                    }
                }

                // 5. SUCCESS REDIRECT
                // We add a query param so React knows the login was successful
                response.sendRedirect(redirectUrl + "?status=success");
            } else {
                // Handle logic-level failures (e.g. status 400 from service)
                response.sendRedirect(redirectUrl + "?status=error&reason=logic_failure");
            }

        } catch (Exception e) {
            log.error("OAuth2 Login Failed: {}", e.getMessage(), e);

            // Send back to frontend with error so the UI can show a toast/alert
            response.sendRedirect(redirectUrl + "?status=error&reason=server_crash");
        }
    }
}