package com.example.MovieBooking.entity;

import com.example.MovieBooking.entity.type.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "show", "showSeats"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false)
    private Timestamp bookingTime;

    @UpdateTimestamp
    private LocalDateTime updatedAt; // ⚡ This will change every time you touch this row

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus; // Ensure this has PENDING, CONFIRMED, CANCELLED

    // ⚡ NEW: To link with Stripe
    private String stripePaymentIntentId;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    // ⚡ Ensure ShowSeat entity has a @ManyToOne Booking booking; field
    @OneToMany(
            mappedBy = "booking",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY
    )
    private List<ShowSeat> showSeats = new ArrayList<>();
}