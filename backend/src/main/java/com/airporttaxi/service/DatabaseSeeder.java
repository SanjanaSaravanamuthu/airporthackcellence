package com.airporttaxi.service;

import com.airporttaxi.domain.entity.Booking;
import com.airporttaxi.domain.entity.RideRequest;
import com.airporttaxi.domain.enums.BookingStatus;
import com.airporttaxi.domain.enums.CarType;
import com.airporttaxi.domain.enums.RequestStatus;
import com.airporttaxi.dto.seed.BookedRideDto;
import com.airporttaxi.dto.seed.OpenRequestDto;
import com.airporttaxi.repository.BookingRepository;
import com.airporttaxi.repository.RideRequestRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Value("${app.data.base-dir:../hackathon-problem-2-airport-taxi-offers/data}")
    private String configuredBaseDir;

    private final BookingRepository bookingRepository;
    private final RideRequestRepository rideRequestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm[:ss]");

    public DatabaseSeeder(BookingRepository bookingRepository, RideRequestRepository rideRequestRepository) {
        this.bookingRepository = bookingRepository;
        this.rideRequestRepository = rideRequestRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        File dataDir = resolveDataDirectory();
        seedBookedRides(new File(dataDir, "problem-2/booked_rides.json"));
        seedOpenRequests(new File(dataDir, "problem-2/open_requests.json"));
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
            if (dir.exists() && dir.isDirectory() && new File(dir, "problem-2/booked_rides.json").exists()) {
                return dir;
            }
        }
        return new File(configuredBaseDir);
    }

    private void seedBookedRides(File file) {
        if (bookingRepository.count() > 0) {
            log.info("Bookings table already contains {} records. Skipping pre-event ride seeding.", bookingRepository.count());
            return;
        }

        if (!file.exists()) {
            log.warn("booked_rides.json does not exist at: {}", file.getAbsolutePath());
            return;
        }

        try {
            List<BookedRideDto> list = objectMapper.readValue(file, new TypeReference<List<BookedRideDto>>() {});
            int index = 1;
            for (BookedRideDto dto : list) {
                String bookingNumber = String.format("100000%04d", index++);
                LocalDateTime start = LocalDateTime.parse(dto.getStart(), FORMATTER);
                LocalDateTime end = LocalDateTime.parse(dto.getEnd(), FORMATTER);

                Booking booking = Booking.builder()
                        .bookingNumber(bookingNumber)
                        .requestId(dto.getRideId())
                        .companyId(dto.getCompanyId())
                        .carId(dto.getVehicleId())
                        .pickupTime(start)
                        .busyStartTime(start)
                        .busyEndTime(end)
                        .status(BookingStatus.CONFIRMED)
                        .isPreBooked(true)
                        .createdAt(LocalDateTime.now())
                        .build();

                bookingRepository.save(booking);
            }
            log.info("Successfully seeded {} pre-event booked rides into Bookings table.", list.size());
        } catch (Exception e) {
            log.error("Failed to seed booked rides: {}", e.getMessage(), e);
        }
    }

    private void seedOpenRequests(File file) {
        if (rideRequestRepository.count() > 0) {
            log.info("RideRequests table already contains {} records. Skipping open requests seeding.", rideRequestRepository.count());
            return;
        }

        if (!file.exists()) {
            log.warn("open_requests.json does not exist at: {}", file.getAbsolutePath());
            return;
        }

        try {
            List<OpenRequestDto> list = objectMapper.readValue(file, new TypeReference<List<OpenRequestDto>>() {});
            for (OpenRequestDto dto : list) {
                CarType carType = null;
                if (dto.getCarType() != null && !dto.getCarType().isBlank()) {
                    try {
                        carType = CarType.valueOf(dto.getCarType().trim().toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                LocalDateTime pickup = LocalDateTime.parse(dto.getPickup(), FORMATTER);

                RideRequest request = RideRequest.builder()
                        .requestId(dto.getRequestId())
                        .travelerName(dto.getTravelerName())
                        .flightId(dto.getFlightId())
                        .hotelId(dto.getHotelId())
                        .adults(dto.getAdults())
                        .childAges(dto.getChildAges() != null ? dto.getChildAges() : List.of())
                        .largeBags(dto.getLargeBags())
                        .smallBags(dto.getSmallBags())
                        .pickupTime(pickup)
                        .carType(carType)
                        .priceInr(dto.getPriceInr())
                        .status(RequestStatus.OPEN)
                        .createdAt(LocalDateTime.now())
                        .build();

                rideRequestRepository.save(request);
            }
            log.info("Successfully seeded {} initial open requests into RideRequests table.", list.size());
        } catch (Exception e) {
            log.error("Failed to seed open requests: {}", e.getMessage(), e);
        }
    }
}
