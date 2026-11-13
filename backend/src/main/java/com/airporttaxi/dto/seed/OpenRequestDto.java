package com.airporttaxi.dto.seed;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class OpenRequestDto {
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
    private List<Integer> childAges = new ArrayList<>();

    @JsonProperty("large_bags")
    private int largeBags;

    @JsonProperty("small_bags")
    private int smallBags;

    private String pickup;

    @JsonProperty("car_type")
    private String carType;

    @JsonProperty("price_inr")
    private Integer priceInr;

    public OpenRequestDto() {}

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

    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }

    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }

    public Integer getPriceInr() { return priceInr; }
    public void setPriceInr(Integer priceInr) { this.priceInr = priceInr; }
}
