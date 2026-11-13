package com.airporttaxi.repository;

import com.airporttaxi.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    List<Booking> findByCompanyId(String companyId);

    List<Booking> findByCarId(String carId);

    /**
     * Rule R6 & M6: Overlap check.
     * Two busy intervals [startA, endA) and [startB, endB) overlap iff startA < endB and endA > startB.
     * Utilizing the composite index on (car_id, busy_start_time, busy_end_time), MySQL performs a B-Tree
     * range scan in O(log N) time complexity.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.carId = :carId
          AND b.status = com.airporttaxi.domain.enums.BookingStatus.CONFIRMED
          AND b.busyStartTime < :newEndTime
          AND b.busyEndTime > :newStartTime
    """)
    long countOverlappingBusyTimes(
        @Param("carId") String carId,
        @Param("newStartTime") LocalDateTime newStartTime,
        @Param("newEndTime") LocalDateTime newEndTime
    );

    /**
     * Overlap check excluding a specific booking ID (used when validating R9 Change Requests on the same booking).
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.carId = :carId
          AND b.id <> :excludeBookingId
          AND b.status = com.airporttaxi.domain.enums.BookingStatus.CONFIRMED
          AND b.busyStartTime < :newEndTime
          AND b.busyEndTime > :newStartTime
    """)
    long countOverlappingBusyTimesExcludingBooking(
        @Param("carId") String carId,
        @Param("newStartTime") LocalDateTime newStartTime,
        @Param("newEndTime") LocalDateTime newEndTime,
        @Param("excludeBookingId") Long excludeBookingId
    );
}

