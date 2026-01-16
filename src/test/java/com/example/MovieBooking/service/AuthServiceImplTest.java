package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import com.example.MovieBooking.repository.UserRepository;
import com.example.MovieBooking.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// This initializes all the @Mock and @InjectMocks annotations
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    // 1. Create Mocks for all dependencies
    // These are "dummy" versions of the classes that AuthServiceImpl needs
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    // 2. This is the class we are actually testing
    // Mockito will "inject" all the mocks above into this instance
    @InjectMocks
    private AuthServiceImpl authService;

    // 3. An ArgumentCaptor to "catch" the object that is saved
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private RegisterDto registerDto;

    @BeforeEach
    void setUp() {
        // Create a standard DTO to use in our tests
        registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setRoles(Set.of("ROLE_USER"));
    }

    // TEST 1: The "Happy Path" - Successful Registration
    @Test
    void testRegister_WhenUserDoesNotExist_ShouldSaveUserAndReturnSuccess() {
        // --- ARRANGE (Given) ---
        // We tell our mocks how to behave.

        // 1. When the service checks if user exists, return "false" (empty)
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // 2. When the service encodes the password, return a fake hash
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword123");

        // 3. When the service saves the user, just return what was given
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // --- ACT (When) ---
        // We call the method we are testing
        String response = authService.register(registerDto);

        // --- ASSERT (Then) ---
        // We check if the results are correct

        // 1. Check the response string
        assertThat(response).isEqualTo("User registered successfully.");

        // 2. Verify that userRepository.save() was called exactly 1 time
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        // 3. "Catch" the User object that was passed to the save() method
        User savedUser = userArgumentCaptor.getValue();

        // 4. Check that the User object was built correctly
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
        assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(savedUser.getRoles()).contains(Role.ROLE_USER);
    }

    // TEST 2: The "Sad Path" - User already exists
    @Test
    void testRegister_WhenEmailExists_ShouldThrowException() {
        // --- ARRANGE (Given) ---
        // 1. When the service checks for username, return "false" (empty)
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // 2. When the service checks for email, return "true" (a real user)
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        // --- ACT & ASSERT (When & Then) ---
        // We check that the *correct exception* is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerDto);
        });

        // 1. Check the exception message
        assertThat(exception.getMessage()).isEqualTo("Email is already taken!");

        // 2. Verify that the save() method was NEVER called
        verify(userRepository, never()).save(any(User.class));
    }
}