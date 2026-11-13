package com.airporttaxi.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteDto {
    private String airport;
    private String city;
    private String area;

    @JsonProperty("distance_km")
    private Double distanceKm;

    @JsonProperty("drive_minutes")
    private Integer driveMinutes;

    @JsonProperty("price_min_inr")
    private Integer priceMinInr;

    @JsonProperty("price_max_inr")
    private Integer priceMaxInr;

    public RouteDto() {}

    public static String toKey(String airport, String city, String area) {
        return (airport + "#" + city + "#" + area).toUpperCase();
    }

    public String getAirport() { return airport; }
    public void setAirport(String airport) { this.airport = airport; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Integer getDriveMinutes() { return driveMinutes; }
    public void setDriveMinutes(Integer driveMinutes) { this.driveMinutes = driveMinutes; }

    public Integer getPriceMinInr() { return priceMinInr; }
    public void setPriceMinInr(Integer priceMinInr) { this.priceMinInr = priceMinInr; }

    public Integer getPriceMaxInr() { return priceMaxInr; }
    public void setPriceMaxInr(Integer priceMaxInr) { this.priceMaxInr = priceMaxInr; }
}
