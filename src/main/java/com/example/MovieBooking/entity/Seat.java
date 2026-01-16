package com.example.MovieBooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "screen") // Exclude relationship
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber; // e.g., "A1", "F12"

    @Column(nullable = false)
    private String seatType; // e.g., "REGULAR", "PREMIUM"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

}