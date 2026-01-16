package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long id);

    @Transactional
    @Modifying
    @Query("DELETE FROM Seat s WHERE s.screen.id = :screenId")
    void deleteByScreenId(@Param("screenId")Long screenId);
}
