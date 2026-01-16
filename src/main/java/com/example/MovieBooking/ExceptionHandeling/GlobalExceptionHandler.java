package com.example.MovieBooking.ExceptionHandeling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle Specific Exceptions (like User not found)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDetailsDto> handleUsernameNotFoundException(
            UsernameNotFoundException exception,
            WebRequest webRequest){

        ErrorDetailsDto errorDetails = new ErrorDetailsDto(
                LocalDateTime.now(),
                exception.getMessage(),
                webRequest.getDescription(false) // This gets the URL (e.g., 'uri=/api/auth/login')
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OAuth2UserException.class)
    public ResponseEntity<ErrorDetailsDto> handleOAuth2UserException(
            OAuth2UserException exception,
            WebRequest webRequest) {

        ErrorDetailsDto errorDetails = new ErrorDetailsDto(
                LocalDateTime.now(),
                exception.getMessage(), // This will be "This account is linked with Google..."
                webRequest.getDescription(false)
        );

        // Using UNAUTHORIZED (401) is best for login issues
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    // 2. Handle Security Exceptions (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetailsDto> handleAccessDeniedException(
            AccessDeniedException exception,
            WebRequest webRequest){

        ErrorDetailsDto errorDetails = new ErrorDetailsDto(
                LocalDateTime.now(),
                "You do not have permission to access this resource.",
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    // 3. Handle Generic RuntimeExceptions (e.g., your "Movie not found" error)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetailsDto> handleRuntimeException(
            RuntimeException exception,
            WebRequest webRequest){

        ErrorDetailsDto errorDetails = new ErrorDetailsDto(
                LocalDateTime.now(),
                exception.getMessage(), // This will show "Movie not found" etc.
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    // 4. Handle All Other Exceptions (Global catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetailsDto> handleGlobalException(
            Exception exception,
            WebRequest webRequest){
        //ya abhi add kia hai
        System.err.println("CRITICAL ENTRY ERROR: " + exception.getMessage());
        exception.printStackTrace(); // This MUST show up in your console now

        ErrorDetailsDto errorDetails = new ErrorDetailsDto(
                LocalDateTime.now(),
                "An internal server error occurred.",
                webRequest.getDescription(false)
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}