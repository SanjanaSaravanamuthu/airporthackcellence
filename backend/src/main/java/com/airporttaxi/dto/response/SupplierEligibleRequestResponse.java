package com.airporttaxi.dto.response;

import com.airporttaxi.dto.catalog.VehicleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SupplierEligibleRequestResponse {

    private RideRequestResponse request;

    @JsonProperty("eligible_vehicles")
    private List<VehicleDto> eligibleVehicles;

    @JsonProperty("price_min_inr")
    private int priceMinInr;

    @JsonProperty("price_max_inr")
    private int priceMaxInr;

    @JsonProperty("drive_minutes")
    private int driveMinutes;

    public SupplierEligibleRequestResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private RideRequestResponse request;
        private List<VehicleDto> eligibleVehicles;
        private int priceMinInr;
        private int priceMaxInr;
        private int driveMinutes;

        public Builder request(RideRequestResponse request) { this.request = request; return this; }
        public Builder eligibleVehicles(List<VehicleDto> eligibleVehicles) { this.eligibleVehicles = eligibleVehicles; return this; }
        public Builder priceMinInr(int priceMinInr) { this.priceMinInr = priceMinInr; return this; }
        public Builder priceMaxInr(int priceMaxInr) { this.priceMaxInr = priceMaxInr; return this; }
        public Builder driveMinutes(int driveMinutes) { this.driveMinutes = driveMinutes; return this; }

        public SupplierEligibleRequestResponse build() {
            SupplierEligibleRequestResponse s = new SupplierEligibleRequestResponse();
            s.request = request;
            s.eligibleVehicles = eligibleVehicles;
            s.priceMinInr = priceMinInr;
            s.priceMaxInr = priceMaxInr;
            s.driveMinutes = driveMinutes;
            return s;
        }
    }

    public RideRequestResponse getRequest() { return request; }
    public void setRequest(RideRequestResponse request) { this.request = request; }

    public List<VehicleDto> getEligibleVehicles() { return eligibleVehicles; }
    public void setEligibleVehicles(List<VehicleDto> eligibleVehicles) { this.eligibleVehicles = eligibleVehicles; }

    public int getPriceMinInr() { return priceMinInr; }
    public void setPriceMinInr(int priceMinInr) { this.priceMinInr = priceMinInr; }

    public int getPriceMaxInr() { return priceMaxInr; }
    public void setPriceMaxInr(int priceMaxInr) { this.priceMaxInr = priceMaxInr; }

    public int getDriveMinutes() { return driveMinutes; }
    public void setDriveMinutes(int driveMinutes) { this.driveMinutes = driveMinutes; }
}
