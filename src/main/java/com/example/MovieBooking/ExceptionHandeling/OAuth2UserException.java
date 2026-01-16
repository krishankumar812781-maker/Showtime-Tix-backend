package com.example.MovieBooking.ExceptionHandeling;

import org.springframework.security.core.AuthenticationException;

public class OAuth2UserException extends AuthenticationException {
    public OAuth2UserException(String msg) {
        super(msg);
    }
}
