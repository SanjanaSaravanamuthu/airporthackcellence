package com.airporttaxi.dto.response;

import com.airporttaxi.domain.enums.CarType;
import com.airporttaxi.domain.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class RideRequestResponse {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("traveler_name")
    private String travelerName;

    @JsonProperty("flight_id")
    private String flightId;

    @JsonProperty("hotel_id")
    private String hotelId;

    private int adults;

    @JsonProperty("child_ages")
    private List<Integer> childAges;

    @JsonProperty("large_bags")
    private int largeBags;

    @JsonProperty("small_bags")
    private int smallBags;

    @JsonProperty("pickup_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime pickupTime;

    @JsonProperty("car_type")
    private CarType carType;

    @JsonProperty("price_inr")
    private int priceInr;

    private RequestStatus status;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime createdAt;

    public RideRequestResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String requestId;
        private String travelerName;
        private String flightId;
        private String hotelId;
        private int adults;
        private List<Integer> childAges;
        private int largeBags;
        private int smallBags;
        private LocalDateTime pickupTime;
        private CarType carType;
        private int priceInr;
        private RequestStatus status;
        private LocalDateTime createdAt;

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder travelerName(String travelerName) { this.travelerName = travelerName; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder hotelId(String hotelId) { this.hotelId = hotelId; return this; }
        public Builder adults(int adults) { this.adults = adults; return this; }
        public Builder childAges(List<Integer> childAges) { this.childAges = childAges; return this; }
        public Builder largeBags(int largeBags) { this.largeBags = largeBags; return this; }
        public Builder smallBags(int smallBags) { this.smallBags = smallBags; return this; }
        public Builder pickupTime(LocalDateTime pickupTime) { this.pickupTime = pickupTime; return this; }
        public Builder carType(CarType carType) { this.carType = carType; return this; }
        public Builder priceInr(int priceInr) { this.priceInr = priceInr; return this; }
        public Builder status(RequestStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RideRequestResponse build() {
            RideRequestResponse r = new RideRequestResponse();
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
            return r;
        }
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
}
