package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    // 1. Find all shows for a specific screen
    List<Show> findByScreenId(Long screenId);

    // 2. Find all shows for a specific movie
    List<Show> findShowByMovieId(Long movieId);

    // 3. Find shows for a movie within a specific time range (Used for Date filtering)
    // Spring Data JPA translates "Between" into: startTime >= ?1 AND startTime <= ?2
    List<Show> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime start, LocalDateTime end);

    // ⚡ Fetches all shows associated with a specific movie ID
    List<Show> findByMovieId(Long movieId);

    @Query("SELECT s FROM Show s " +
            "WHERE (:movieId IS NULL OR s.movie.id = :movieId) " +
            "AND (:city IS NULL OR s.screen.theater.city = :city) " +
            "AND (:theaterId IS NULL OR s.screen.theater.id = :theaterId) " +
            "AND (:start IS NULL OR s.startTime >= :start) " +
            "AND (:end IS NULL OR s.startTime <= :end)")
    List<Show> findFilteredShows(
            @Param("movieId") Long movieId,
            @Param("city") String city,
            @Param("theaterId") Long theaterId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ⚡ Check if any show on this screen has seats that are NOT "AVAILABLE"
    @Query("SELECT COUNT(s) > 0 FROM Show s JOIN s.showSeat ss " +
            "WHERE s.screen.id = :screenId AND ss.status != 'AVAILABLE'")
    boolean existsByScreenIdAndActiveBookings(@Param("screenId") Long screenId);

    @Modifying
    @Query("DELETE FROM Show s WHERE s.screen.id = :screenId")
    void deleteByScreenId(@Param("screenId") Long screenId);

    // 4. Find shows for any movie within a specific time range
    List<Show> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
}