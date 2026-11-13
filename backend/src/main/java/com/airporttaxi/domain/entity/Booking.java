package com.airporttaxi.domain.entity;

import com.airporttaxi.domain.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    // Composite index for O(log N) car availability overlap checks (Rules R6 & M6)
    @Index(name = "idx_booking_car_busy_interval", columnList = "car_id, busy_start_time, busy_end_time"),
    @Index(name = "idx_booking_number", columnList = "booking_number", unique = true)
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_number", length = 10, unique = true, nullable = false)
    private String bookingNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private RideOffer rideOffer;

    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "company_id", nullable = false, length = 32)
    private String companyId;

    @Column(name = "car_id", nullable = false, length = 32)
    private String carId;

    @Column(name = "flight_id", length = 32)
    private String flightId;

    @Column(name = "hotel_id", length = 32)
    private String hotelId;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Column(name = "busy_start_time", nullable = false)
    private LocalDateTime busyStartTime;

    @Column(name = "busy_end_time", nullable = false)
    private LocalDateTime busyEndTime;

    @Column(name = "price_inr")
    private int priceInr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "is_pre_booked", nullable = false)
    private boolean isPreBooked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Booking() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String bookingNumber;
        private RideOffer rideOffer;
        private String requestId;
        private String companyId;
        private String carId;
        private String flightId;
        private String hotelId;
        private LocalDateTime pickupTime;
        private LocalDateTime busyStartTime;
        private LocalDateTime busyEndTime;
        private int priceInr;
        private BookingStatus status;
        private boolean isPreBooked;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder bookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; return this; }
        public Builder rideOffer(RideOffer rideOffer) { this.rideOffer = rideOffer; return this; }
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder companyId(String companyId) { this.companyId = companyId; return this; }
        public Builder carId(String carId) { this.carId = carId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder hotelId(String hotelId) { this.hotelId = hotelId; return this; }
        public Builder pickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; return this; }
        public Builder busyStartTime(LocalDateTime busyStartTime) { this.busyStartTime = busyStartTime; return this; }
        public Builder busyEndTime(LocalDateTime busyEndTime) { this.busyEndTime = busyEndTime; return this; }
        public Builder priceInr(int priceInr) { this.priceInr = priceInr; return this; }
        public Builder status(BookingStatus status) { this.status = status; return this; }
        public Builder isPreBooked(boolean isPreBooked) { this.isPreBooked = isPreBooked; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Booking build() {
            Booking b = new Booking();
            b.id = id;
            b.bookingNumber = bookingNumber;
            b.rideOffer = rideOffer;
            b.requestId = requestId;
            b.companyId = companyId;
            b.carId = carId;
            b.flightId = flightId;
            b.hotelId = hotelId;
            b.pickupTime = pickupTime;
            b.busyStartTime = busyStartTime;
            b.busyEndTime = busyEndTime;
            b.priceInr = priceInr;
            b.status = status;
            b.isPreBooked = isPreBooked;
            b.createdAt = createdAt;
            return b;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public RideOffer getRideOffer() { return rideOffer; }
    public void setRideOffer(RideOffer rideOffer) { this.rideOffer = rideOffer; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCarId() { return carId; }
    public void setCarId(String carId) { this.carId = carId; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }

    public LocalDateTime getBusyStartTime() { return busyStartTime; }
    public void setBusyStartTime(LocalDateTime busyStartTime) { this.busyStartTime = busyStartTime; }

    public LocalDateTime getBusyEndTime() { return busyEndTime; }
    public void setBusyEndTime(LocalDateTime busyEndTime) { this.busyEndTime = busyEndTime; }

    public int getPriceInr() { return priceInr; }
    public void setPriceInr(int priceInr) { this.priceInr = priceInr; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public boolean isPreBooked() { return isPreBooked; }
    public void setPreBooked(boolean preBooked) { isPreBooked = preBooked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
