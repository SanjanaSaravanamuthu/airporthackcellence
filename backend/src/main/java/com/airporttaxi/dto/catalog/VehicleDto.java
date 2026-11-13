package com.airporttaxi.dto.catalog;

import com.airporttaxi.domain.enums.CarType;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VehicleDto {
    @JsonProperty("vehicle_id")
    private String vehicleId;

    private CarType type;

    private int seats;

    @JsonProperty("large_bags")
    private int largeBags;

    @JsonProperty("small_bags")
    private int smallBags;

    public VehicleDto() {}

    public VehicleDto(String vehicleId, CarType type, int seats, int largeBags, int smallBags) {
        this.vehicleId = vehicleId;
        this.type = type;
        this.seats = seats;
        this.largeBags = largeBags;
        this.smallBags = smallBags;
    }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public CarType getType() { return type; }
    public void setType(CarType type) { this.type = type; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public int getLargeBags() { return largeBags; }
    public void setLargeBags(int largeBags) { this.largeBags = largeBags; }

    public int getSmallBags() { return smallBags; }
    public void setSmallBags(int smallBags) { this.smallBags = smallBags; }
}
