package com.example.MovieBooking.entity;

import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "bookings")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = true) // Null for Google OAuth users
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private AuthProvider providerType;

    private String providerId;


    // --- ADDED NEW MULTI-ROLE FIELD ---
    @ElementCollection(fetch = FetchType.EAGER) // EAGER is often best for security roles
    @CollectionTable(
            name = "user_roles", // This will be the name of the new table
            joinColumns = @JoinColumn(name = "user_id") // Foreign key column to this User
    )
    @Column(name = "role", nullable = false, length = 30) // This is the column that will store the role string
    @Enumerated(EnumType.STRING) // Store roles as "ROLE_USER", "ROLE_ADMIN"
    private Set<Role> roles = new HashSet<>();

    @Column(length = 512) // Give it enough space
    private String refreshToken;

    private Instant refreshTokenExpiry;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false) //kuch nahi to Local to hoga
    private AuthProvider authProvider;

    //Relationships
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private List<Booking> bookings = new ArrayList<>();


}