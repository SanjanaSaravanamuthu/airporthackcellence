package com.airporttaxi.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ChangeRequestInput {

    @JsonProperty("new_flight_id")
    private String newFlightId;

    @JsonProperty("new_pickup_time")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime newPickupTime;

    public ChangeRequestInput() {}

    public String getNewFlightId() { return newFlightId; }
    public void setNewFlightId(String newFlightId) { this.newFlightId = newFlightId; }

    public LocalDateTime getNewPickupTime() { return newPickupTime; }
    public void setNewPickupTime(LocalDateTime newPickupTime) { this.newPickupTime = newPickupTime; }
}
