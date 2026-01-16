package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String userEmail);
    Optional<User> findByUsername(String username);
    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProvider providerType);
}
