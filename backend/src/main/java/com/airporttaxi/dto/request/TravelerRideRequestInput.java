package com.airporttaxi.dto.request;

import com.airporttaxi.domain.enums.CarType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TravelerRideRequestInput {

    @NotBlank(message = "Traveler name is required")
    @JsonProperty("traveler_name")
    private String travelerName;

    @NotBlank(message = "Flight ID is required")
    @JsonProperty("flight_id")
    private String flightId;

    @NotBlank(message = "Hotel ID is required")
    @JsonProperty("hotel_id")
    private String hotelId;

    @Min(value = 1, message = "At least 1 adult is required")
    private int adults;

    @JsonProperty("child_ages")
    private List<Integer> childAges = new ArrayList<>();

    @Min(value = 0, message = "Large bags cannot be negative")
    @JsonProperty("large_bags")
    private int largeBags;

    @Min(value = 0, message = "Small bags cannot be negative")
    @JsonProperty("small_bags")
    private int smallBags;

    @JsonProperty("pickup")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime pickup;

    @JsonProperty("car_type")
    private CarType carType;

    @NotNull(message = "Price in INR is required")
    @JsonProperty("price_inr")
    private Integer priceInr;

    public TravelerRideRequestInput() {}

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

    public LocalDateTime getPickup() { return pickup; }
    public void setPickup(LocalDateTime pickup) { this.pickup = pickup; }

    public CarType getCarType() { return carType; }
    public void setCarType(CarType carType) { this.carType = carType; }

    public Integer getPriceInr() { return priceInr; }
    public void setPriceInr(Integer priceInr) { this.priceInr = priceInr; }
}
