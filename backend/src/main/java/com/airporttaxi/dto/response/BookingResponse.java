package com.airporttaxi.dto.response;

import com.airporttaxi.domain.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class BookingResponse {

    private Long id;

    @JsonProperty("booking_number")
    private String bookingNumber;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("company_id")
    private String companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("car_id")
    private String carId;

    @JsonProperty("flight_id")
    private String flightId;

    @JsonProperty("hotel_id")
    private String hotelId;

    @JsonProperty("pickup_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime pickupTime;

    @JsonProperty("busy_start_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime busyStartTime;

    @JsonProperty("busy_end_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime busyEndTime;

    @JsonProperty("price_inr")
    private int priceInr;

    private BookingStatus status;

    @JsonProperty("is_pre_booked")
    private boolean isPreBooked;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime createdAt;

    public BookingResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String bookingNumber;
        private String requestId;
        private String companyId;
        private String companyName;
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
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder companyId(String companyId) { this.companyId = companyId; return this; }
        public Builder companyName(String companyName) { this.companyName = companyName; return this; }
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

        public BookingResponse build() {
            BookingResponse b = new BookingResponse();
            b.id = id;
            b.bookingNumber = bookingNumber;
            b.requestId = requestId;
            b.companyId = companyId;
            b.companyName = companyName;
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

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

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
