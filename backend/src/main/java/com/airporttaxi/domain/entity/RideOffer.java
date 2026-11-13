package com.airporttaxi.domain.entity;

import com.airporttaxi.domain.enums.OfferStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ride_offers", indexes = {
    @Index(name = "idx_offer_request_id", columnList = "request_id"),
    @Index(name = "idx_offer_company_id", columnList = "company_id")
})
public class RideOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    @JsonIgnoreProperties({"offers", "hibernateLazyInitializer", "handler"})
    private RideRequest rideRequest;

    @Column(name = "company_id", nullable = false, length = 32)
    private String companyId;

    @Column(name = "vehicle_id", nullable = false, length = 32)
    private String vehicleId;

    @Column(name = "offered_price_inr", nullable = false)
    private int offeredPriceInr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public RideOffer() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private RideRequest rideRequest;
        private String companyId;
        private String vehicleId;
        private int offeredPriceInr;
        private OfferStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder rideRequest(RideRequest rideRequest) { this.rideRequest = rideRequest; return this; }
        public Builder companyId(String companyId) { this.companyId = companyId; return this; }
        public Builder vehicleId(String vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder offeredPriceInr(int offeredPriceInr) { this.offeredPriceInr = offeredPriceInr; return this; }
        public Builder status(OfferStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RideOffer build() {
            RideOffer o = new RideOffer();
            o.id = id;
            o.rideRequest = rideRequest;
            o.companyId = companyId;
            o.vehicleId = vehicleId;
            o.offeredPriceInr = offeredPriceInr;
            o.status = status;
            o.createdAt = createdAt;
            return o;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RideRequest getRideRequest() { return rideRequest; }
    public void setRideRequest(RideRequest rideRequest) { this.rideRequest = rideRequest; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public int getOfferedPriceInr() { return offeredPriceInr; }
    public void setOfferedPriceInr(int offeredPriceInr) { this.offeredPriceInr = offeredPriceInr; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
