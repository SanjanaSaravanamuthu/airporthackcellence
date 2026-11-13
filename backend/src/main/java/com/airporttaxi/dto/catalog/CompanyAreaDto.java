package com.airporttaxi.dto.catalog;

public class CompanyAreaDto {
    private String city;
    private String area;

    public CompanyAreaDto() {}

    public CompanyAreaDto(String city, String area) {
        this.city = city;
        this.area = area;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}
