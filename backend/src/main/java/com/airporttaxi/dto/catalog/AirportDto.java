package com.airporttaxi.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AirportDto {
    private String code;
    private String name;
    private String city;
    private String country;
    private String timezone;
    private Double lat;
    private Double lng;

    @JsonProperty("serves_cities")
    private List<String> servesCities = new ArrayList<>();

    public AirportDto() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public List<String> getServesCities() { return servesCities; }
    public void setServesCities(List<String> servesCities) { this.servesCities = servesCities; }
}
