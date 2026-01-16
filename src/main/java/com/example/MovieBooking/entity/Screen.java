package com.example.MovieBooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"theater", "seats", "shows"}) // Exclude all relationships
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Audi 1", "IMAX"

    @Column(nullable = false)
    private String screenType; // e.g., "IMAX", "3D", "REGULAR"

    //relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @OneToMany(
            mappedBy = "screen",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(
            mappedBy = "screen",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<Show> shows = new ArrayList<>();
}