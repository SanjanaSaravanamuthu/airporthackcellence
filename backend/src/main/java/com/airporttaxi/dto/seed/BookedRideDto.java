package com.airporttaxi.dto.seed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookedRideDto {
    @JsonProperty("ride_id")
    private String rideId;

    @JsonProperty("company_id")
    private String companyId;

    @JsonProperty("vehicle_id")
    private String vehicleId;

    private String start;
    private String end;

    public BookedRideDto() {}

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }
}
