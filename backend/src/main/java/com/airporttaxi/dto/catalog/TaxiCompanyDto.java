package com.airporttaxi.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TaxiCompanyDto {
    @JsonProperty("company_id")
    private String companyId;

    private String name;
    private List<String> airports = new ArrayList<>();
    private List<CompanyAreaDto> areas = new ArrayList<>();

    @JsonProperty("service_from")
    private String serviceFrom;

    @JsonProperty("service_until")
    private String serviceUntil;

    private Double rating;
    private List<VehicleDto> vehicles = new ArrayList<>();

    public TaxiCompanyDto() {}

    public boolean isOpenAt(LocalTime time) {
        if (serviceFrom == null || serviceUntil == null) {
            return true;
        }
        LocalTime from = LocalTime.parse(serviceFrom);
        LocalTime until = LocalTime.parse(serviceUntil);

        if (!from.isAfter(until)) {
            return !time.isBefore(from) && !time.isAfter(until);
        } else {
            return !time.isBefore(from) || !time.isAfter(until);
        }
    }

    public boolean servesAirport(String airportCode) {
        if (airports == null || airportCode == null) return false;
        return airports.stream().anyMatch(a -> a.equalsIgnoreCase(airportCode));
    }

    public boolean servesArea(String city, String area) {
        if (areas == null || city == null || area == null) return false;
        return areas.stream().anyMatch(a ->
            a.getCity() != null && a.getCity().equalsIgnoreCase(city) &&
            a.getArea() != null && a.getArea().equalsIgnoreCase(area)
        );
    }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getAirports() { return airports; }
    public void setAirports(List<String> airports) { this.airports = airports; }

    public List<CompanyAreaDto> getAreas() { return areas; }
    public void setAreas(List<CompanyAreaDto> areas) { this.areas = areas; }

    public String getServiceFrom() { return serviceFrom; }
    public void setServiceFrom(String serviceFrom) { this.serviceFrom = serviceFrom; }

    public String getServiceUntil() { return serviceUntil; }
    public void setServiceUntil(String serviceUntil) { this.serviceUntil = serviceUntil; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public List<VehicleDto> getVehicles() { return vehicles; }
    public void setVehicles(List<VehicleDto> vehicles) { this.vehicles = vehicles; }
}
