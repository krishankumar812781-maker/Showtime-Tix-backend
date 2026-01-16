package com.example.MovieBooking.dto.RequestDto;


import lombok.Data;

@Data
public class LoginDto {
    private String usernameOrEmail;
    private String password;
}
