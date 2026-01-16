package com.example.MovieBooking.service;
import com.example.MovieBooking.ExceptionHandeling.OAuth2UserException;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    //UserDetails object is created by loading user data from the database
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {

        // 1. Load your User entity from the database
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));

        // Check if the user is a Social-Only user
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // Throw a custom exception that the controller/handler can catch
            throw new OAuth2UserException("This account is linked with " + user.getAuthProvider() + ". Please login using Social Login.");
        }

        // 2. Convert your Set<Role> into a Set<GrantedAuthority>
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());

        // 3. Return a new Spring Security User object ( UserDetails )
        //To get the roles into the JWT, they must first exist inside the Authentication object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}