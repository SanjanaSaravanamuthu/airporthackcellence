package com.airporttaxi.dto.catalog;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class FlightDto {
    @JsonProperty("flight_id")
    private String flightId;

    private String airline;

    @JsonProperty("flight_number")
    private String flightNumber;

    private String from;
    private String to;

    @JsonProperty("departure_local")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureLocal;

    @JsonProperty("arrival_local")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalLocal;

    private Integer stops;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    private String cabin;

    public FlightDto() {}

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public LocalDateTime getDepartureLocal() { return departureLocal; }
    public void setDepartureLocal(LocalDateTime departureLocal) { this.departureLocal = departureLocal; }

    public LocalDateTime getArrivalLocal() { return arrivalLocal; }
    public void setArrivalLocal(LocalDateTime arrivalLocal) { this.arrivalLocal = arrivalLocal; }

    public Integer getStops() { return stops; }
    public void setStops(Integer stops) { this.stops = stops; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getCabin() { return cabin; }
    public void setCabin(String cabin) { this.cabin = cabin; }
}
