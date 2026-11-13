package com.airporttaxi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SupplierOfferInput {

    @NotBlank(message = "Request ID is required")
    @JsonProperty("request_id")
    private String requestId;

    @NotBlank(message = "Company ID is required")
    @JsonProperty("company_id")
    private String companyId;

    @NotBlank(message = "Vehicle ID is required")
    @JsonProperty("vehicle_id")
    private String vehicleId;

    @NotNull(message = "Offered price in INR is required")
    @Min(value = 1, message = "Offered price must be positive")
    @JsonProperty("offered_price_inr")
    private Integer offeredPriceInr;

    public SupplierOfferInput() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public Integer getOfferedPriceInr() { return offeredPriceInr; }
    public void setOfferedPriceInr(Integer offeredPriceInr) { this.offeredPriceInr = offeredPriceInr; }
}
