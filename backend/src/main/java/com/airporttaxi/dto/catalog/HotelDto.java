package com.airporttaxi.dto.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class HotelDto {
    @JsonProperty("hotel_id")
    private String hotelId;

    private String name;
    private List<String> aliases = new ArrayList<>();
    private String city;
    private String area;
    private Double lat;
    private Double lng;
    private Double rating;
    private Integer stars;

    @JsonProperty("check_in_from")
    private String checkInFrom;

    @JsonProperty("check_out_until")
    private String checkOutUntil;

    @JsonProperty("family_friendly")
    private Boolean familyFriendly;

    public HotelDto() {}

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getAliases() { return aliases; }
    public void setAliases(List<String> aliases) { this.aliases = aliases; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }

    public String getCheckInFrom() { return checkInFrom; }
    public void setCheckInFrom(String checkInFrom) { this.checkInFrom = checkInFrom; }

    public String getCheckOutUntil() { return checkOutUntil; }
    public void setCheckOutUntil(String checkOutUntil) { this.checkOutUntil = checkOutUntil; }

    public Boolean getFamilyFriendly() { return familyFriendly; }
    public void setFamilyFriendly(Boolean familyFriendly) { this.familyFriendly = familyFriendly; }
}
