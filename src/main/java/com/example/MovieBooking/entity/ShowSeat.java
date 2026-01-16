package com.example.MovieBooking.entity;
import com.example.MovieBooking.entity.type.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"show", "seat", "booking"}) // Exclude all relationships
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status; // e.g., "AVAILABLE", "LOCKED", "BOOKED","HOLD"

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime lastUpdated; // Update this every time status changes

    // ⚡ This hook automatically updates the timestamp before saving to DB
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
    //relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = true) // Nullable!
    private Booking booking;
}