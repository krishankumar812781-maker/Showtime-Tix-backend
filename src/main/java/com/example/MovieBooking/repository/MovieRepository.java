package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

     Optional<List<Movie>> findByGenre(String genre);

     Optional<List<Movie>> findByLanguage(String language);

    // ⚡ This handles the naive matching: SQL "WHERE LOWER(title) LIKE %query%"
    List<Movie> findByTitleContainingIgnoreCase(String title);



}
