package com.example.MovieBooking.service;
import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.UserResponseDto;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import com.example.MovieBooking.repository.UserRepository;
import com.example.MovieBooking.security.JwtTokenProvider;
import com.example.MovieBooking.security.CookieService; // ⚡ Added CookieService
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService; // ⚡ Injected centralized CookieService


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public ResponseEntity<String> login(LoginDto loginDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // --refresh token store kr dete hai DB ma
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(refreshToken).toInstant());
        userRepository.save(user);

        // ⚡ Using CookieService for cleaner code
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString())
                .body("Login successful. Tokens stored in secure cookies.");
    }

    @Override
    @Transactional
    public String register(RegisterDto registerDto) {
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL);

        Set<Role> roles = new HashSet<>();
        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            roles = registerDto.getRoles().stream()
                    .map(roleStr -> Role.valueOf(roleStr.toUpperCase()))
                    .collect(Collectors.toSet());
        } else {
            roles.add(Role.ROLE_USER);
        }
        user.setRoles(roles);
        userRepository.save(user);

        return "User registered successfully.";
    }

    @Override
    @Transactional
    public ResponseEntity<String> refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String requestRefreshToken = refreshTokenRequestDto.getRefreshToken();

        User user = userRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found."));

        if (user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        String rolesStr = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        //-- roles bhi pass krne padenge to create token with roles
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), rolesStr);
        String newRefreshToken = jwtTokenProvider.generateRefreshTokenFromEmail(user.getEmail(), rolesStr);

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(newRefreshToken).toInstant());
        userRepository.save(user);

        // ⚡ Centralized Cookie generation on Refresh
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(newAccessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(newRefreshToken).toString())
                .body("Tokens refreshed successfully.");
    }

    @Override
    @Transactional
    public ResponseEntity<String> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {
        // 1. Determine AuthProvider type(enum) , ProviderId and Email
        // 2. save user info in DB
        // 3. if user already has account then directly login
        // 4. otherwise first Register then login
        AuthProvider providerType = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> AuthProvider.LOCAL;
        };

        //Dekhna padega alag alag provider ke hisaab sa kis attribute ko use karna hai unique id ke liye
        String providerId = switch (registrationId.toLowerCase()) {
            case "google" -> (String) oAuth2User.getAttribute("sub");
            case "github", "facebook", "discord" -> oAuth2User.getAttribute("id").toString();
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };

        String email = oAuth2User.getAttribute("email");

        // 1. Try to find user by Provider ID and Type
        User user =userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);
        //exception nahi throw krna kyuki nahi hai to create krna hai naya

        // 2. If not found by ID, check by Email (Account Linking)
        if (user == null && email != null) {
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                // Link this social provider to the existing account
                user.setProviderId(providerId);
                user.setAuthProvider(providerType);
                user = userRepository.save(user);
            }
        }

        // 3. Register flow if still not found
        if (user == null) {
            user = new User();
            // Fallback for missing email (e.g., github-user-123@noemail.com)
            user.setEmail(email != null ? email : providerId + "@" + registrationId.toLowerCase() + ".com");
            user.setUsername(determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId));
            user.setAuthProvider(providerType);
            user.setProviderId(providerId);
            user.setPassword(""); // OAuth users don't have local passwords
            user.setRoles(Set.of(Role.ROLE_USER));
            user = userRepository.save(user);
        }

        // 4. Token & Cookie Logic (Same as your snippet)
        String rolesStr = user.getRoles().stream().map(Enum::name).collect(Collectors.joining(","));
        String accessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), rolesStr);
        String refreshToken = jwtTokenProvider.generateRefreshTokenFromEmail(user.getEmail(), rolesStr);

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(refreshToken).toInstant());
        userRepository.save(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.createAccessTokenCookie(accessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.createRefreshTokenCookie(refreshToken).toString())
                .body("OAuth2 login successful.");


    }


    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) return email;

        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }

    @Override
    public UserResponseDto getCurrentUser() {
        // 1. Hn kyuki ek baar andar anee ke baad to hum kahi sa bhi user information le sakte hai  SecurityContext sa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check specifically for anonymousUser string
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Logged In");
        }

        // 2. Extract details
        String email = authentication.getName();
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new UserResponseDto(email, roles);
    }


    @Override
    @Transactional
    public ResponseEntity<String> logout() {
        // 1. Identify the user from the current SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                // 2. Invalidate the refresh token in the Database database ma ab kya kaam uska
                user.setRefreshToken(null);
                user.setRefreshTokenExpiry(null);
                userRepository.save(user);
                log.info("Refresh token cleared for user: {}", auth.getName());
            });
        }

        // 3. Instruct browser to delete both cookies
        ResponseCookie accessCookie = cookieService.deleteCookie("accessToken");
        ResponseCookie refreshCookie = cookieService.deleteCookie("refreshToken");

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out successfully");
    }
}