package com.airporttaxi.dto.response;

import com.airporttaxi.domain.enums.CarType;
import com.airporttaxi.domain.enums.OfferStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class RideOfferResponse {

    private Long id;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("company_id")
    private String companyId;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("vehicle_id")
    private String vehicleId;

    @JsonProperty("vehicle_type")
    private CarType vehicleType;

    private int seats;

    @JsonProperty("large_bags")
    private int largeBags;

    @JsonProperty("small_bags")
    private int smallBags;

    @JsonProperty("offered_price_inr")
    private int offeredPriceInr;

    private OfferStatus status;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime createdAt;

    public RideOfferResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String requestId;
        private String companyId;
        private String companyName;
        private String vehicleId;
        private CarType vehicleType;
        private int seats;
        private int largeBags;
        private int smallBags;
        private int offeredPriceInr;
        private OfferStatus status;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder companyId(String companyId) { this.companyId = companyId; return this; }
        public Builder companyName(String companyName) { this.companyName = companyName; return this; }
        public Builder vehicleId(String vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder vehicleType(CarType vehicleType) { this.vehicleType = vehicleType; return this; }
        public Builder seats(int seats) { this.seats = seats; return this; }
        public Builder largeBags(int largeBags) { this.largeBags = largeBags; return this; }
        public Builder smallBags(int smallBags) { this.smallBags = smallBags; return this; }
        public Builder offeredPriceInr(int offeredPriceInr) { this.offeredPriceInr = offeredPriceInr; return this; }
        public Builder status(OfferStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public RideOfferResponse build() {
            RideOfferResponse r = new RideOfferResponse();
            r.id = id;
            r.requestId = requestId;
            r.companyId = companyId;
            r.companyName = companyName;
            r.vehicleId = vehicleId;
            r.vehicleType = vehicleType;
            r.seats = seats;
            r.largeBags = largeBags;
            r.smallBags = smallBags;
            r.offeredPriceInr = offeredPriceInr;
            r.status = status;
            r.createdAt = createdAt;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public CarType getVehicleType() { return vehicleType; }
    public void setVehicleType(CarType vehicleType) { this.vehicleType = vehicleType; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public int getLargeBags() { return largeBags; }
    public void setLargeBags(int largeBags) { this.largeBags = largeBags; }

    public int getSmallBags() { return smallBags; }
    public void setSmallBags(int smallBags) { this.smallBags = smallBags; }

    public int getOfferedPriceInr() { return offeredPriceInr; }
    public void setOfferedPriceInr(int offeredPriceInr) { this.offeredPriceInr = offeredPriceInr; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
