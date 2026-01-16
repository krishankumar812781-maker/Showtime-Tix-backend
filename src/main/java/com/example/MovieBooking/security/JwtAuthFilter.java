package com.example.MovieBooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // Added for cookie handling
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthFilter(JwtTokenProvider tokenProvider,
                         UserDetailsService userDetailsService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Now fetches token from Cookie instead of just the Header
            String token = getJwtFromRequest(request);

            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                // Token is valid, set authentication in context
                //extract username from token
                String username = tokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //Even though the user didn't provide a password this time (they provided a JWT) ,
                // for every request we need to create an authentication token to fill security context
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }

    /**
     * Extracts the JWT from either the "accessToken" cookie or the Authorization header.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Try to get the token from Cookies (Primary for Web)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) { // Must match your CookieService name
                    return cookie.getValue();
                }
            }
        }

        // 2. Fallback: Try to get from Authorization Header (Useful for Postman testing)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}