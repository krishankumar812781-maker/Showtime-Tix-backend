package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.BookingResponseDto;
import com.example.MovieBooking.dto.RequestDto.BookingRequestDto;
import com.example.MovieBooking.entity.Booking;
import com.example.MovieBooking.entity.Show;
import com.example.MovieBooking.entity.ShowSeat;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.BookingStatus;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.BookingRepository;
import com.example.MovieBooking.repository.ShowRepository;
import com.example.MovieBooking.repository.ShowSeatRepository;
import com.example.MovieBooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final ShowSeatRepository showSeatRepository;
    // ⚡ KafkaTemplate REMOVED

    // ⚡ Stripe Service injection
    private final StripeService stripeService;

    @Transactional
    public BookingResponseDto createBooking(String userEmail, BookingRequestDto bookingRequestDto) {
        // 1. Find the User and Show
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        Show show = showRepository.findById(bookingRequestDto.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        // 2. Lock, Validate, and Collect Seats
        List<ShowSeat> lockedSeats = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long showSeatId : bookingRequestDto.getShowSeatIds()) {

            // --- THIS IS THE CRITICAL SECTION (Pessimistic Lock) ---
            ShowSeat seat = showSeatRepository.findByIdAndLock(showSeatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + showSeatId));

            // --- VALIDATION ---
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new RuntimeException("Seat " + seat.getSeat().getSeatNumber() + " is not for this show.");
            }

            // ---Check if seat is AVAILABLE , agar HOLD , BOOkED hai to nahi kar sakte---
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getSeat().getSeatNumber() + " is already booked or locked.");
            }

            lockedSeats.add(seat);
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // 3. Create the Booking (Status PENDING for Stripe flow)
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(Timestamp.from(Instant.now()));
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(BookingStatus.PENDING);

        // ⚡ Stripe Payment Intent Logic
        String clientSecret = null;
        try{
            // Save first to get the ID for Metadata
            Booking savedBooking = bookingRepository.save(booking);

            var intent = stripeService.createPaymentIntent(totalAmount, "usd", savedBooking.getId().toString());

            // Store Stripe Intent ID in the entity
            savedBooking.setStripePaymentIntentId(intent.getId());
            clientSecret = intent.getClientSecret();

            // 4. Update Seats and Link to Booking (Marking as HELD to hold them during payment)
            for (ShowSeat seat : lockedSeats) {
                seat.setStatus(SeatStatus.HELD);
                seat.setBooking(savedBooking);
            }
            showSeatRepository.saveAll(lockedSeats);

            // ⚡ Kafka send REMOVED

            // 6. Map to a Response DTO and return with Client Secret
            BookingResponseDto response = mapToBookingResponseDto(savedBooking, lockedSeats);
            response.setClientSecret(clientSecret);
            return response;

        } catch (Exception e) {
            System.err.println("CRITICAL BOOKING ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment Initialization Failed: " + e.getMessage());
        }
    }

    @Transactional
    public BookingResponseDto confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 1. Update Booking Status
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        // 2. Ensure seats are permanently marked as BOOKED
        List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
        for (ShowSeat seat : seats) {
            seat.setStatus(SeatStatus.BOOKED);
        }

        Booking confirmedBooking = bookingRepository.save(booking);

        // ⚡ Kafka send REMOVED (Confirmation now handled by DB transaction)

        return mapToBookingResponseDto(confirmedBooking, seats);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        return bookings.stream()
                .map(booking -> {
                    List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
                    return mapToBookingResponseDto(booking, seats);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to this user"));

        List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());

        return mapToBookingResponseDto(booking, seats);
    }

    @Transactional
    public BookingResponseDto cancelBooking(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to this user"));

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        List<ShowSeat> seatsToRelease = showSeatRepository.findByBookingId(booking.getId());

        for (ShowSeat seat : seatsToRelease) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }

        showSeatRepository.saveAll(seatsToRelease);

        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        // ⚡ Kafka send REMOVED

        return mapToBookingResponseDto(cancelledBooking, seatsToRelease);
    }

    private BookingResponseDto mapToBookingResponseDto(Booking booking, List<ShowSeat> seats) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingTime(booking.getBookingTime());
        dto.setTotalAmount(booking.getTotalAmount());

        Show show = booking.getShow();
        dto.setMovieTitle(show.getMovie().getTitle());
        dto.setTheaterName(show.getScreen().getTheater().getName());
        dto.setScreenName(show.getScreen().getName());
        dto.setShowStartTime(show.getStartTime());

        List<String> seatNumbers = seats.stream()
                .map(showSeat -> showSeat.getSeat().getSeatNumber())
                .toList();
        dto.setBookedSeats(seatNumbers);

        return dto;
    }
}