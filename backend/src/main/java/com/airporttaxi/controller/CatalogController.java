package com.airporttaxi.controller;

import com.airporttaxi.dto.catalog.*;
import com.airporttaxi.exception.RuleViolationException;
import com.airporttaxi.service.CatalogCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@CrossOrigin(origins = "*")
public class CatalogController {

    private final CatalogCacheService catalogCacheService;

    public CatalogController(CatalogCacheService catalogCacheService) {
        this.catalogCacheService = catalogCacheService;
    }

    @GetMapping("/airports")
    public ResponseEntity<Collection<AirportDto>> getAirports() {
        return ResponseEntity.ok(catalogCacheService.getAllAirports());
    }

    @GetMapping("/hotels")
    public ResponseEntity<Collection<HotelDto>> getHotels() {
        return ResponseEntity.ok(catalogCacheService.getAllHotels());
    }

    @GetMapping("/hotels/by-airport/{airportCode}")
    public ResponseEntity<List<HotelDto>> getHotelsByAirport(@PathVariable String airportCode) {
        if (catalogCacheService.findAirport(airportCode).isEmpty()) {
            throw new RuleViolationException("R1", "Airport '" + airportCode + "' not found");
        }
        return ResponseEntity.ok(catalogCacheService.getHotelsByAirport(airportCode));
    }

    @GetMapping("/flights")
    public ResponseEntity<Collection<FlightDto>> getFlights() {
        return ResponseEntity.ok(catalogCacheService.getAllFlights());
    }

    @GetMapping("/companies")
    public ResponseEntity<Collection<TaxiCompanyDto>> getCompanies() {
        return ResponseEntity.ok(catalogCacheService.getAllCompanies());
    }

    @GetMapping("/routes/lookup")
    public ResponseEntity<RouteDto> getRoute(
            @RequestParam String airport,
            @RequestParam String city,
            @RequestParam String area) {
        RouteDto route = catalogCacheService.findRoute(airport, city, area)
                .orElseThrow(() -> new RuleViolationException("R3", String.format(
                        "Route not found from airport '%s' to '%s / %s'", airport, city, area)));
        return ResponseEntity.ok(route);
    }
}
