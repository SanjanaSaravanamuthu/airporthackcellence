package com.airporttaxi.dto.response;

import com.airporttaxi.domain.enums.ChangeRequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ChangeRequestResponse {

    private Long id;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("booking_number")
    private String bookingNumber;

    @JsonProperty("new_flight_id")
    private String newFlightId;

    @JsonProperty("new_pickup_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime newPickupTime;

    private ChangeRequestStatus status;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime createdAt;

    @JsonProperty("resolved_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime resolvedAt;

    public ChangeRequestResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long bookingId;
        private String bookingNumber;
        private String newFlightId;
        private LocalDateTime newPickupTime;
        private ChangeRequestStatus status;
        private String rejectionReason;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder bookingId(Long bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; return this; }
        public Builder newFlightId(String newFlightId) { this.newFlightId = newFlightId; return this; }
        public Builder newPickupTime(LocalDateTime newPickupTime) { this.newPickupTime = newPickupTime; return this; }
        public Builder status(ChangeRequestStatus status) { this.status = status; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public ChangeRequestResponse build() {
            ChangeRequestResponse r = new ChangeRequestResponse();
            r.id = id;
            r.bookingId = bookingId;
            r.bookingNumber = bookingNumber;
            r.newFlightId = newFlightId;
            r.newPickupTime = newPickupTime;
            r.status = status;
            r.rejectionReason = rejectionReason;
            r.createdAt = createdAt;
            r.resolvedAt = resolvedAt;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }

    public String getNewFlightId() { return newFlightId; }
    public void setNewFlightId(String newFlightId) { this.newFlightId = newFlightId; }

    public LocalDateTime getNewPickupTime() { return newPickupTime; }
    public void setNewPickupTime(LocalDateTime newPickupTime) { this.newPickupTime = newPickupTime; }

    public ChangeRequestStatus getStatus() { return status; }
    public void setStatus(ChangeRequestStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
