package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Booking;
import com.example.MovieBooking.entity.type.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByUserId(Long id);

    Optional<Booking> findByIdAndUserId(Long bookingId, Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.bookingStatus = 'EXPIRED' " +
            "WHERE b.bookingStatus = 'PENDING' AND b.updatedAt < :threshold")
    int expirePendingBookings(@Param("threshold") LocalDateTime threshold);

    // Helper to find IDs of bookings we just expired to clean up their seats
    List<Booking> findByBookingStatusAndBookingTimeBefore(BookingStatus status, LocalDateTime threshold);

    // This will find all bookings associated with a specific show ID.
    List<Booking> findAllByShowId(Long showId);

}
