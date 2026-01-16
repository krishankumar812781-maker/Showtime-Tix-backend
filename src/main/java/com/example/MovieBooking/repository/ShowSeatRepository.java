package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.ShowSeat;
import com.example.MovieBooking.entity.type.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat,Long> {


    List<ShowSeat> findByShowId(Long showId);

    /**
     * 2. THE MISSING METHOD
     * Checks if any seat for a specific show is already "BOOKED".
     * This prevents deleting a show that people have already paid for.
     */
    boolean existsByShowIdAndStatus(Long showId, SeatStatus status);

    // If you also want to update them in one go using a single SQL query (More efficient)
    @Modifying
    @Transactional
    @Query("UPDATE ShowSeat s SET s.status = 'AVAILABLE', s.booking = null " +
            "WHERE s.status = :status AND s.lastUpdated < :threshold")
    int bulkReleaseSeats(@Param("status") SeatStatus status, @Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE ShowSeat s SET s.status = 'AVAILABLE', s.booking = null " +
            "WHERE s.booking.id IN :bookingIds")
    void releaseSeatsByBookingIds(@Param("bookingIds") List<Long> bookingIds);

    /**
     * This is the pessimistic locking method.
     * When called inside a @Transactional method, it will execute
     * a "SELECT ... FOR UPDATE" SQL query, locking the row
     * until the transaction is committed or rolled back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.id = :id")
    Optional<ShowSeat> findByIdAndLock(@Param("id") Long id);

    List<ShowSeat> findByBookingId(Long id);
}
