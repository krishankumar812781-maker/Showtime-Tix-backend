package com.example.MovieBooking.service;

import com.example.MovieBooking.entity.Booking;
import com.example.MovieBooking.entity.ShowSeat;
import com.example.MovieBooking.entity.type.BookingStatus;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.BookingRepository;
import com.example.MovieBooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCleanupService {

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;

    // Runs every 1 minute
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void cleanupHoldSeats() {
        LocalDateTime tenMinsAgo = LocalDateTime.now().minusMinutes(10);

        // Using the bulk method for better performance
        int count = showSeatRepository.bulkReleaseSeats(SeatStatus.HOLD, tenMinsAgo);

        if (count > 0) {
            log.info("Released {} seats that were stuck on HOLD", count);
        }
    }

    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional // ⚡ Crucial: Ensures both updates succeed or both fail
    public void cleanupExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        // 1. Find the Bookings that are about to expire
        List<Booking> expiredBookings = bookingRepository
                .findByBookingStatusAndBookingTimeBefore(BookingStatus.PENDING, threshold);

        if (!expiredBookings.isEmpty()) {
            List<Long> bookingIds = expiredBookings.stream()
                    .map(Booking::getId)
                    .toList();

            // 2. Release all seats associated with these Booking IDs
            showSeatRepository.releaseSeatsByBookingIds(bookingIds);

            // 3. Mark the Bookings as EXPIRED
            bookingRepository.expirePendingBookings(threshold);

            log.info("Cleanup Complete: Expired {} bookings and released associated seats.", bookingIds.size());
        }
    }
}