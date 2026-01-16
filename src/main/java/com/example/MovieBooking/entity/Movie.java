package com.example.MovieBooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_language", columnList = "language"),
        @Index(name = "idx_movie_genre", columnList = "genre")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"show"})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // ⚡ Changed from description to plot and used TEXT for unlimited length
    @Column(columnDefinition = "TEXT")
    private String plot;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String duration; // Changed to String to match OMDb "148 min"

    private String posterUrl;

    // ⚡ New fields from OMDb
    private String director;

    @Column(length = 1000) // Actors lists can be long
    private String actors;

    private String rating; // Storing imdbRating (e.g., "8.8")

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Show> show = new ArrayList<>();
}