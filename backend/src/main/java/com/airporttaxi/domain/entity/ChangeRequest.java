package com.airporttaxi.domain.entity;

import com.airporttaxi.domain.enums.ChangeRequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_requests", indexes = {
    @Index(name = "idx_change_req_booking", columnList = "booking_id")
})
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Booking booking;

    @Column(name = "new_flight_id", length = 32)
    private String newFlightId;

    @Column(name = "new_pickup_time", nullable = false)
    private LocalDateTime newPickupTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeRequestStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public ChangeRequest() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Booking booking;
        private String newFlightId;
        private LocalDateTime newPickupTime;
        private ChangeRequestStatus status;
        private String rejectionReason;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder booking(Booking booking) { this.booking = booking; return this; }
        public Builder newFlightId(String newFlightId) { this.newFlightId = newFlightId; return this; }
        public Builder newPickupTime(LocalDateTime newPickupTime) { this.newPickupTime = newPickupTime; return this; }
        public Builder status(ChangeRequestStatus status) { this.status = status; return this; }
        public Builder rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public ChangeRequest build() {
            ChangeRequest c = new ChangeRequest();
            c.id = id;
            c.booking = booking;
            c.newFlightId = newFlightId;
            c.newPickupTime = newPickupTime;
            c.status = status;
            c.rejectionReason = rejectionReason;
            c.createdAt = createdAt;
            c.resolvedAt = resolvedAt;
            return c;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

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
