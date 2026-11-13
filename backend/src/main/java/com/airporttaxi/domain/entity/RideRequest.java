package com.airporttaxi.domain.entity;

import com.airporttaxi.domain.enums.CarType;
import com.airporttaxi.domain.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ride_requests", indexes = {
    @Index(name = "idx_request_status", columnList = "status")
})
public class RideRequest {

    @Id
    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "traveler_name", nullable = false)
    private String travelerName;

    @Column(name = "flight_id", nullable = false, length = 32)
    private String flightId;

    @Column(name = "hotel_id", nullable = false, length = 32)
    private String hotelId;

    @Column(nullable = false)
    private int adults;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "request_child_ages", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "age")
    private List<Integer> childAges = new ArrayList<>();

    @Column(name = "large_bags", nullable = false)
    private int largeBags;

    @Column(name = "small_bags", nullable = false)
    private int smallBags;

    @Column(name = "pickup_time", nullable = false)
    private LocalDateTime pickupTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", length = 20)
    private CarType carType;

    @Column(name = "price_inr", nullable = false)
    private int priceInr;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "rideRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RideOffer> offers = new ArrayList<>();

    public RideRequest() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String requestId;
        private String travelerName;
        private String flightId;
        private String hotelId;
        private int adults;
        private List<Integer> childAges = new ArrayList<>();
        private int largeBags;
        private int smallBags;
        private LocalDateTime pickupTime;
        private CarType carType;
        private int priceInr;
        private RequestStatus status;
        private LocalDateTime createdAt;
        private List<RideOffer> offers = new ArrayList<>();

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder travelerName(String travelerName) { this.travelerName = travelerName; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder hotelId(String hotelId) { this.hotelId = hotelId; return this; }
        public Builder adults(int adults) { this.adults = adults; return this; }
        public Builder childAges(List<Integer> childAges) { this.childAges = childAges != null ? childAges : new ArrayList<>(); return this; }
        public Builder largeBags(int largeBags) { this.largeBags = largeBags; return this; }
        public Builder smallBags(int smallBags) { this.smallBags = smallBags; return this; }
        public Builder pickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; return this; }
        public Builder carType(CarType carType) { this.carType = carType; return this; }
        public Builder priceInr(int priceInr) { this.priceInr = priceInr; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder offers(List<RideOffer> offers) { this.offers = offers; return this; }

        public RideRequest build() {
            RideRequest r = new RideRequest();
            r.requestId = requestId;
            r.travelerName = travelerName;
            r.flightId = flightId;
            r.hotelId = hotelId;
            r.adults = adults;
            r.childAges = childAges;
            r.largeBags = largeBags;
            r.smallBags = smallBags;
            r.pickupTime = pickupTime;
            r.carType = carType;
            r.priceInr = priceInr;
            r.status = status;
            r.createdAt = createdAt;
            r.offers = offers;
            return r;
        }
    }

    public int getTotalPassengers() {
        return adults + (childAges != null ? childAges.size() : 0);
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getTravelerName() { return travelerName; }
    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public int getAdults() { return adults; }
    public void setAdults(int adults) { this.adults = adults; }

    public List<Integer> getChildAges() { return childAges; }
    public void setChildAges(List<Integer> childAges) { this.childAges = childAges; }

    public int getLargeBags() { return largeBags; }
    public void setLargeBags(int largeBags) { this.largeBags = largeBags; }

    public int getSmallBags() { return smallBags; }
    public void setSmallBags(int smallBags) { this.smallBags = smallBags; }

    public LocalDateTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; }

    public CarType getCarType() { return carType; }
    public void setCarType(CarType carType) { this.carType = carType; }

    public int getPriceInr() { return priceInr; }
    public void setPriceInr(int priceInr) { this.priceInr = priceInr; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<RideOffer> getOffers() { return offers; }
    public void setOffers(List<RideOffer> offers) { this.offers = offers; }
}
