package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.BookingRequestDto;
import com.example.MovieBooking.entity.ShowSeat;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceIntegrationTest {

    // We are injecting the REAL service and repositories
    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private UserRepository userRepository; // Needed for the assert block

    // We MUST mock Kafka, or the test will try to connect and fail

    //private KafkaTemplate<String, String> kafkaTemplate;

    // We no longer need a @BeforeEach setup,
    // because src/test/resources/data.sql is doing it for us.

    @Test
    @Transactional // Keeps session open for lazy loading in asserts
    void testCreateBooking_ConcurrencyTest_ShouldPreventDoubleBooking() throws InterruptedException {

        // --- ARRANGE ---
        // These IDs and emails come from your data.sql file
        long showId = 1L;
        long seatToBookId = 1L; // This is ShowSeat ID 1 (A1)
        String user1Email = "user@example.com";
        String user2Email = "admin@example.com"; // Admin is also a user

        // Request for User 1
        BookingRequestDto user1Request = new BookingRequestDto();
        user1Request.setShowId(showId);
        user1Request.setShowSeatIds(List.of(seatToBookId));

        // Request for User 2 (for the *same seat*)
        BookingRequestDto user2Request = new BookingRequestDto();
        user2Request.setShowId(showId);
        user2Request.setShowSeatIds(List.of(seatToBookId));

        // Tools to manage concurrency
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // --- ACT ---
        // Because createBooking() is marked @Transactional(propagation = REQUIRES_NEW),
        // each of these threads will run in its OWN new transaction,
        // which is what allows them to compete for the pessimistic lock.

        // Thread for User 1
        executor.submit(() -> {
            try {
                latch.await(); // Wait for the "go" signal
                bookingService.createBooking(user1Email, user1Request);
            } catch (Exception e) {
                // If this fails, increment the count
                failureCount.incrementAndGet();
            }
        });

        // Thread for User 2
        executor.submit(() -> {
            try {
                latch.await(); // Wait for the "go" signal
                bookingService.createBooking(user2Email, user2Request);
            } catch (Exception e) {
                // If this fails, increment the count
                failureCount.incrementAndGet();
            }
        });

        // Give threads time to get ready
        Thread.sleep(100);
        latch.countDown(); // "GO!"

        // Wait for both threads to finish
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // --- ASSERT ---

        // 1. Check that exactly ONE of the bookings failed
        assertThat(failureCount.get()).isEqualTo(1);

        // 2. Check the database to see the final state of the seat
        ShowSeat finalSeat = showSeatRepository.findById(seatToBookId)
                .orElseThrow(() -> new RuntimeException("Test setup failed: Seat not found"));

        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(finalSeat.getBooking()).isNotNull();

        // 3. Check who the winner was (fixes LazyInitializationException)
        // We know the test is @Transactional, so the session is still open.
        User winningUser = finalSeat.getBooking().getUser();
        assertThat(winningUser.getEmail()).isIn(user1Email, user2Email);
    }
}