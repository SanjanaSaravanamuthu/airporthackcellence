package com.airporttaxi.service;

import com.airporttaxi.dto.catalog.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CatalogCacheService {

    private static final Logger log = LoggerFactory.getLogger(CatalogCacheService.class);

    @Value("${app.data.base-dir:../hackathon-problem-2-airport-taxi-offers/data}")
    private String configuredBaseDir;

    private final ObjectMapper objectMapper;

    // In-memory HashMaps for O(1) lookups
    private final Map<String, AirportDto> airportMap = new ConcurrentHashMap<>();
    private final Map<String, HotelDto> hotelMap = new ConcurrentHashMap<>();
    private final Map<String, FlightDto> flightMap = new ConcurrentHashMap<>();
    private final Map<String, RouteDto> routeMap = new ConcurrentHashMap<>();
    private final Map<String, TaxiCompanyDto> companyMap = new ConcurrentHashMap<>();
    private final Map<String, VehicleDto> vehicleMap = new ConcurrentHashMap<>();
    private final Map<String, String> vehicleToCompanyMap = new ConcurrentHashMap<>();

    public CatalogCacheService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    public void init() {
        File dataDir = resolveDataDirectory();
        log.info("Loading catalog datasets from: {}", dataDir.getAbsolutePath());

        loadAirports(new File(dataDir, "catalog/airports.json"));
        loadHotels(new File(dataDir, "catalog/hotels.json"));
        loadFlights(new File(dataDir, "catalog/flights.json"));
        loadCompanies(new File(dataDir, "problem-2/taxi_companies.json"));
        loadRoutes(new File(dataDir, "problem-2/routes.json"));

        log.info("Catalog cached successfully: {} airports, {} hotels, {} flights, {} routes, {} companies, {} vehicles",
                airportMap.size(), hotelMap.size(), flightMap.size(), routeMap.size(), companyMap.size(), vehicleMap.size());
    }

    private File resolveDataDirectory() {
        List<String> candidates = List.of(
            configuredBaseDir,
            "../hackathon-problem-2-airport-taxi-offers/data",
            "hackathon-problem-2-airport-taxi-offers/data",
            "C:/Airport_Taxi/hackathon-problem-2-airport-taxi-offers/data"
        );
        for (String candidate : candidates) {
            File dir = Paths.get(candidate).toFile();
            if (dir.exists() && dir.isDirectory() && new File(dir, "catalog/airports.json").exists()) {
                return dir;
            }
        }
        return new File(configuredBaseDir);
    }

    private void loadAirports(File file) {
        if (!file.exists()) {
            log.warn("Airports file does not exist: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<AirportDto> list = objectMapper.readValue(file, new TypeReference<List<AirportDto>>() {});
            for (AirportDto a : list) {
                if (a.getCode() != null) {
                    airportMap.put(a.getCode().toUpperCase(), a);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load airports: {}", e.getMessage(), e);
        }
    }

    private void loadHotels(File file) {
        if (!file.exists()) {
            log.warn("Hotels file does not exist: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<HotelDto> list = objectMapper.readValue(file, new TypeReference<List<HotelDto>>() {});
            for (HotelDto h : list) {
                if (h.getHotelId() != null) {
                    hotelMap.put(h.getHotelId().toUpperCase(), h);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load hotels: {}", e.getMessage(), e);
        }
    }

    private void loadFlights(File file) {
        if (!file.exists()) {
            log.warn("Flights file does not exist: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<FlightDto> list = objectMapper.readValue(file, new TypeReference<List<FlightDto>>() {});
            for (FlightDto f : list) {
                if (f.getFlightId() != null) {
                    flightMap.put(f.getFlightId().toUpperCase(), f);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load flights: {}", e.getMessage(), e);
        }
    }

    private void loadCompanies(File file) {
        if (!file.exists()) {
            log.warn("Taxi companies file does not exist: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<TaxiCompanyDto> list = objectMapper.readValue(file, new TypeReference<List<TaxiCompanyDto>>() {});
            for (TaxiCompanyDto c : list) {
                if (c.getCompanyId() != null) {
                    companyMap.put(c.getCompanyId().toUpperCase(), c);
                    if (c.getVehicles() != null) {
                        for (VehicleDto v : c.getVehicles()) {
                            if (v.getVehicleId() != null) {
                                vehicleMap.put(v.getVehicleId().toUpperCase(), v);
                                vehicleToCompanyMap.put(v.getVehicleId().toUpperCase(), c.getCompanyId());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Failed to load taxi companies: {}", e.getMessage(), e);
        }
    }

    private void loadRoutes(File file) {
        if (!file.exists()) {
            log.warn("Routes file does not exist: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<RouteDto> list = objectMapper.readValue(file, new TypeReference<List<RouteDto>>() {});
            for (RouteDto r : list) {
                String key = RouteDto.toKey(r.getAirport(), r.getCity(), r.getArea());
                routeMap.put(key, r);
            }
        } catch (IOException e) {
            log.error("Failed to load routes: {}", e.getMessage(), e);
        }
    }

    // O(1) Cache lookups
    public Optional<AirportDto> findAirport(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(airportMap.get(code.toUpperCase()));
    }

    public Optional<HotelDto> findHotel(String hotelId) {
        if (hotelId == null) return Optional.empty();
        return Optional.ofNullable(hotelMap.get(hotelId.toUpperCase()));
    }

    public Optional<FlightDto> findFlight(String flightId) {
        if (flightId == null) return Optional.empty();
        return Optional.ofNullable(flightMap.get(flightId.toUpperCase()));
    }

    public Optional<RouteDto> findRoute(String airport, String city, String area) {
        if (airport == null || city == null || area == null) return Optional.empty();
        return Optional.ofNullable(routeMap.get(RouteDto.toKey(airport, city, area)));
    }

    public Optional<TaxiCompanyDto> findCompany(String companyId) {
        if (companyId == null) return Optional.empty();
        return Optional.ofNullable(companyMap.get(companyId.toUpperCase()));
    }

    public Optional<VehicleDto> findVehicle(String vehicleId) {
        if (vehicleId == null) return Optional.empty();
        return Optional.ofNullable(vehicleMap.get(vehicleId.toUpperCase()));
    }

    public Optional<String> findCompanyIdForVehicle(String vehicleId) {
        if (vehicleId == null) return Optional.empty();
        return Optional.ofNullable(vehicleToCompanyMap.get(vehicleId.toUpperCase()));
    }

    public Collection<AirportDto> getAllAirports() {
        return Collections.unmodifiableCollection(airportMap.values());
    }

    public Collection<HotelDto> getAllHotels() {
        return Collections.unmodifiableCollection(hotelMap.values());
    }

    public Collection<FlightDto> getAllFlights() {
        return Collections.unmodifiableCollection(flightMap.values());
    }

    public Collection<TaxiCompanyDto> getAllCompanies() {
        return Collections.unmodifiableCollection(companyMap.values());
    }

    public Collection<RouteDto> getAllRoutes() {
        return Collections.unmodifiableCollection(routeMap.values());
    }
}
