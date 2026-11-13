package com.airporttaxi.service;

import com.airporttaxi.domain.entity.Booking;
import com.airporttaxi.domain.entity.ChangeRequest;
import com.airporttaxi.domain.entity.RideOffer;
import com.airporttaxi.domain.entity.RideRequest;
import com.airporttaxi.domain.enums.BookingStatus;
import com.airporttaxi.domain.enums.ChangeRequestStatus;
import com.airporttaxi.domain.enums.OfferStatus;
import com.airporttaxi.domain.enums.RequestStatus;
import com.airporttaxi.dto.catalog.AirportDto;
import com.airporttaxi.dto.catalog.FlightDto;
import com.airporttaxi.dto.catalog.HotelDto;
import com.airporttaxi.dto.catalog.RouteDto;
import com.airporttaxi.dto.catalog.TaxiCompanyDto;
import com.airporttaxi.dto.catalog.VehicleDto;
import com.airporttaxi.dto.request.ChangeRequestInput;
import com.airporttaxi.dto.request.ResolveChangeRequestInput;
import com.airporttaxi.dto.request.SupplierOfferInput;
import com.airporttaxi.dto.request.TravelerRideRequestInput;
import com.airporttaxi.dto.response.SupplierEligibleRequestResponse;
import com.airporttaxi.dto.mapper.DtoMapper;
import com.airporttaxi.exception.RuleViolationException;
import com.airporttaxi.repository.BookingRepository;
import com.airporttaxi.repository.ChangeRequestRepository;
import com.airporttaxi.repository.RideOfferRepository;
import com.airporttaxi.repository.RideRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final CatalogCacheService catalogCacheService;
    private final RideRequestRepository rideRequestRepository;
    private final RideOfferRepository rideOfferRepository;
    private final BookingRepository bookingRepository;
    private final ChangeRequestRepository changeRequestRepository;
    private final DtoMapper dtoMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    public RideService(CatalogCacheService catalogCacheService,
                       RideRequestRepository rideRequestRepository,
                       RideOfferRepository rideOfferRepository,
                       BookingRepository bookingRepository,
                       ChangeRequestRepository changeRequestRepository,
                       DtoMapper dtoMapper) {
        this.catalogCacheService = catalogCacheService;
        this.rideRequestRepository = rideRequestRepository;
        this.rideOfferRepository = rideOfferRepository;
        this.bookingRepository = bookingRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.dtoMapper = dtoMapper;
    }

    /**
     * Rule R1, R2, R3 & Feature M1:
     * Validate traveler ride request input against in-memory catalog cache in O(1) time
     * and persist RideRequest.
     */
    @Transactional
    public RideRequest createRideRequest(TravelerRideRequestInput input) {
        log.info("Processing ride request creation for traveler: {}", input.getTravelerName());

        // 1. Rule R1: Passenger check
        if (input.getAdults() < 1) {
            throw new RuleViolationException("R1", "At least 1 adult passenger is required");
        }
        if (input.getChildAges() != null) {
            for (Integer age : input.getChildAges()) {
                if (age == null || age < 0 || age > 17) {
                    throw new RuleViolationException("R1", "Child age must be between 0 and 17. Provided: " + age);
                }
            }
        }

        // 2. Rule R1: Flight validation via O(1) cache
        FlightDto flight = catalogCacheService.findFlight(input.getFlightId())
                .orElseThrow(() -> new RuleViolationException("R1", "Flight '" + input.getFlightId() + "' not found in catalog"));

        // 3. Rule R1: Hotel validation via O(1) cache
        HotelDto hotel = catalogCacheService.findHotel(input.getHotelId())
                .orElseThrow(() -> new RuleViolationException("R1", "Hotel '" + input.getHotelId() + "' not found in catalog"));

        // 4. Rule R1: Arrival airport serves hotel's city
        String arrivalAirportCode = flight.getTo();
        AirportDto airport = catalogCacheService.findAirport(arrivalAirportCode)
                .orElseThrow(() -> new RuleViolationException("R1", "Arrival airport '" + arrivalAirportCode + "' not found in catalog"));

        boolean servesCity = airport.getServesCities() != null &&
                airport.getServesCities().stream().anyMatch(city -> city.equalsIgnoreCase(hotel.getCity()));
        if (!servesCity) {
            throw new RuleViolationException("R1", String.format(
                    "Arrival airport '%s' does not serve hotel city '%s' (serves: %s)",
                    arrivalAirportCode, hotel.getCity(), airport.getServesCities()));
        }

        // 5. Rule R2: Pickup timing validation
        LocalDateTime landingTime = flight.getArrivalLocal();
        LocalDateTime earliestPickup = landingTime.plusMinutes(60);
        LocalDateTime pickupTime = input.getPickup();

        if (pickupTime == null) {
            pickupTime = earliestPickup;
        } else if (pickupTime.isBefore(earliestPickup)) {
            throw new RuleViolationException("R2", String.format(
                    "Pickup time (%s) must be at least 60 minutes after landing time (%s). Earliest pickup is %s",
                    pickupTime, landingTime, earliestPickup));
        }

        // 6. Rule R3: Route and suggested price range validation
        RouteDto route = catalogCacheService.findRoute(arrivalAirportCode, hotel.getCity(), hotel.getArea())
                .orElseThrow(() -> new RuleViolationException("R3", String.format(
                        "No route defined from airport '%s' to '%s / %s'",
                        arrivalAirportCode, hotel.getCity(), hotel.getArea())));

        if (input.getPriceInr() < route.getPriceMinInr() || input.getPriceInr() > route.getPriceMaxInr()) {
            throw new RuleViolationException("R3", String.format(
                    "Traveler price ₹%d is outside the suggested route range [₹%d - ₹%d]",
                    input.getPriceInr(), route.getPriceMinInr(), route.getPriceMaxInr()));
        }

        // Persist valid RideRequest
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        RideRequest request = RideRequest.builder()
                .requestId(requestId)
                .travelerName(input.getTravelerName())
                .flightId(flight.getFlightId())
                .hotelId(hotel.getHotelId())
                .adults(input.getAdults())
                .childAges(input.getChildAges() != null ? input.getChildAges() : List.of())
                .largeBags(input.getLargeBags())
                .smallBags(input.getSmallBags())
                .pickupTime(pickupTime)
                .carType(input.getCarType())
                .priceInr(input.getPriceInr())
                .status(RequestStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        RideRequest saved = rideRequestRepository.save(request);
        log.info("Ride request created successfully: {}", saved.getRequestId());
        return saved;
    }

    /**
     * Rules R4, R5, R6, R7 & Feature M3:
     * Validate supplier car assignment and price, then persist RideOffer.
     */
    @Transactional
    public RideOffer submitRideOffer(SupplierOfferInput input) {
        log.info("Supplier {} submitting offer for request {} with vehicle {}",
                input.getCompanyId(), input.getRequestId(), input.getVehicleId());

        RideRequest request = rideRequestRepository.findById(input.getRequestId())
                .orElseThrow(() -> new RuleViolationException("R7", "Ride request '" + input.getRequestId() + "' not found"));

        if (request.getStatus() != RequestStatus.OPEN) {
            throw new RuleViolationException("R7", "Ride request is not open for offers. Current status: " + request.getStatus());
        }

        TaxiCompanyDto company = catalogCacheService.findCompany(input.getCompanyId())
                .orElseThrow(() -> new RuleViolationException("R4", "Taxi company '" + input.getCompanyId() + "' not found"));

        VehicleDto vehicle = catalogCacheService.findVehicle(input.getVehicleId())
                .orElseThrow(() -> new RuleViolationException("R5", "Vehicle '" + input.getVehicleId() + "' not found"));

        String vehicleCompany = catalogCacheService.findCompanyIdForVehicle(input.getVehicleId()).orElse(null);
        if (vehicleCompany == null || !vehicleCompany.equalsIgnoreCase(input.getCompanyId())) {
            throw new RuleViolationException("R7", String.format(
                    "Vehicle '%s' does not belong to supplier company '%s'",
                    input.getVehicleId(), input.getCompanyId()));
        }

        FlightDto flight = catalogCacheService.findFlight(request.getFlightId())
                .orElseThrow(() -> new RuleViolationException("R1", "Flight '" + request.getFlightId() + "' not found"));
        HotelDto hotel = catalogCacheService.findHotel(request.getHotelId())
                .orElseThrow(() -> new RuleViolationException("R1", "Hotel '" + request.getHotelId() + "' not found"));
        RouteDto route = catalogCacheService.findRoute(flight.getTo(), hotel.getCity(), hotel.getArea())
                .orElseThrow(() -> new RuleViolationException("R3", "Route not found for airport " + flight.getTo() + " to " + hotel.getCity()));

        // Rule R4 Validation
        if (!company.servesAirport(flight.getTo())) {
            throw new RuleViolationException("R4", String.format(
                    "Supplier '%s' does not operate at arrival airport '%s'", company.getName(), flight.getTo()));
        }

        if (!company.servesArea(hotel.getCity(), hotel.getArea())) {
            throw new RuleViolationException("R4", String.format(
                    "Supplier '%s' does not drive to '%s / %s'", company.getName(), hotel.getCity(), hotel.getArea()));
        }

        LocalTime pickupClockTime = request.getPickupTime().toLocalTime();
        if (!company.isOpenAt(pickupClockTime)) {
            throw new RuleViolationException("R4", String.format(
                    "Supplier '%s' is closed at pickup time %s (Service hours: %s - %s)",
                    company.getName(), pickupClockTime, company.getServiceFrom(), company.getServiceUntil()));
        }

        // Rule R5 Validation
        int totalPassengers = request.getTotalPassengers();
        if (vehicle.getSeats() < totalPassengers) {
            throw new RuleViolationException("R5", String.format(
                    "Vehicle '%s' has %d seats, but request requires %d seats",
                    vehicle.getVehicleId(), vehicle.getSeats(), totalPassengers));
        }

        if (vehicle.getLargeBags() < request.getLargeBags()) {
            throw new RuleViolationException("R5", String.format(
                    "Vehicle '%s' accommodates %d large bags, but request has %d large bags",
                    vehicle.getVehicleId(), vehicle.getLargeBags(), request.getLargeBags()));
        }

        if (vehicle.getSmallBags() < request.getSmallBags()) {
            throw new RuleViolationException("R5", String.format(
                    "Vehicle '%s' accommodates %d small bags, but request has %d small bags",
                    vehicle.getVehicleId(), vehicle.getSmallBags(), request.getSmallBags()));
        }

        if (request.getCarType() != null && vehicle.getType() != request.getCarType()) {
            throw new RuleViolationException("R5", String.format(
                    "Vehicle type '%s' does not match requested car type '%s'",
                    vehicle.getType(), request.getCarType()));
        }

        // Rule R6 Validation
        LocalDateTime busyStartTime = request.getPickupTime();
        LocalDateTime busyEndTime = busyStartTime.plusMinutes(2L * route.getDriveMinutes());
        long overlapCount = bookingRepository.countOverlappingBusyTimes(vehicle.getVehicleId(), busyStartTime, busyEndTime);
        if (overlapCount > 0) {
            throw new RuleViolationException("R6", String.format(
                    "Vehicle '%s' is already busy during interval [%s - %s]",
                    vehicle.getVehicleId(), busyStartTime, busyEndTime));
        }

        // Rule R7 Validation
        if (input.getOfferedPriceInr() < route.getPriceMinInr() || input.getOfferedPriceInr() > route.getPriceMaxInr()) {
            throw new RuleViolationException("R7", String.format(
                    "Offered price ₹%d is outside the route range [₹%d - ₹%d]",
                    input.getOfferedPriceInr(), route.getPriceMinInr(), route.getPriceMaxInr()));
        }

        // Persist RideOffer
        RideOffer offer = RideOffer.builder()
                .rideRequest(request)
                .companyId(company.getCompanyId())
                .vehicleId(vehicle.getVehicleId())
                .offeredPriceInr(input.getOfferedPriceInr())
                .status(OfferStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        RideOffer saved = rideOfferRepository.save(offer);
        log.info("Ride offer ID {} submitted successfully by supplier {}", saved.getId(), saved.getCompanyId());
        return saved;
    }

    /**
     * Rules R6, R8 & Mandatory Feature M6: Confirm booking atomically.
     */
    @Transactional
    public Booking confirmBooking(Long offerId) {
        log.info("Attempting to confirm booking for offer ID: {}", offerId);

        RideOffer offer = rideOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuleViolationException("R8", "Offer not found with ID: " + offerId));

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new RuleViolationException("R8", "Offer is not in OPEN status. Current status: " + offer.getStatus());
        }

        RideRequest request = offer.getRideRequest();
        if (request.getStatus() != RequestStatus.OPEN) {
            throw new RuleViolationException("R8", "Ride request is no longer open. Current status: " + request.getStatus());
        }

        FlightDto flight = catalogCacheService.findFlight(request.getFlightId())
                .orElseThrow(() -> new RuleViolationException("R1", "Flight '" + request.getFlightId() + "' not found"));
        HotelDto hotel = catalogCacheService.findHotel(request.getHotelId())
                .orElseThrow(() -> new RuleViolationException("R1", "Hotel '" + request.getHotelId() + "' not found"));
        RouteDto route = catalogCacheService.findRoute(flight.getTo(), hotel.getCity(), hotel.getArea())
                .orElseThrow(() -> new RuleViolationException("R3", "Route not found for flight arrival and hotel area"));

        LocalDateTime busyStartTime = request.getPickupTime();
        LocalDateTime busyEndTime = busyStartTime.plusMinutes(2L * route.getDriveMinutes());

        // Rule M6 Overlap check
        long overlappingCount = bookingRepository.countOverlappingBusyTimes(
                offer.getVehicleId(), busyStartTime, busyEndTime);

        if (overlappingCount > 0) {
            log.warn("Double booking prevented for car {} during window [{} - {}]",
                    offer.getVehicleId(), busyStartTime, busyEndTime);
            throw new RuleViolationException("M6", String.format(
                    "Car '%s' is no longer free. Overlapping busy time detected for window [%s - %s] (Rule M6: Double booking prevented)",
                    offer.getVehicleId(), busyStartTime, busyEndTime));
        }

        String bookingNumber = generateUnique10DigitBookingNumber();

        offer.setStatus(OfferStatus.BOOKED);
        rideOfferRepository.save(offer);

        rideOfferRepository.markOtherOffersStatus(request.getRequestId(), offer.getId(), OfferStatus.NOT_CHOSEN);

        request.setStatus(RequestStatus.FULFILLED);
        rideRequestRepository.save(request);

        Booking booking = Booking.builder()
                .bookingNumber(bookingNumber)
                .rideOffer(offer)
                .requestId(request.getRequestId())
                .companyId(offer.getCompanyId())
                .carId(offer.getVehicleId())
                .flightId(request.getFlightId())
                .hotelId(request.getHotelId())
                .pickupTime(request.getPickupTime())
                .busyStartTime(busyStartTime)
                .busyEndTime(busyEndTime)
                .priceInr(offer.getOfferedPriceInr())
                .status(BookingStatus.CONFIRMED)
                .isPreBooked(false)
                .createdAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking confirmed successfully. Booking Number: {}, Car: {}", bookingNumber, savedBooking.getCarId());
        return savedBooking;
    }

    /**
     * Rule R8: Traveler rejects an offer.
     */
    @Transactional
    public RideOffer rejectOffer(Long offerId) {
        RideOffer offer = rideOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuleViolationException("R8", "Offer not found with ID: " + offerId));

        if (offer.getStatus() != OfferStatus.OPEN) {
            throw new RuleViolationException("R8", "Only OPEN offers can be rejected. Current status: " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.REJECTED);
        return rideOfferRepository.save(offer);
    }

    /**
     * Rules R9 & M7: Traveler submits a flight or pickup change request.
     */
    @Transactional
    public ChangeRequest submitChangeRequest(Long bookingId, ChangeRequestInput input) {
        log.info("Submitting change request for booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuleViolationException("R9", "Booking not found with ID: " + bookingId));

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuleViolationException("R9", "Cannot request changes for booking with status: " + booking.getStatus());
        }

        if ((input.getNewFlightId() == null || input.getNewFlightId().isBlank()) && input.getNewPickupTime() == null) {
            throw new RuleViolationException("R9", "Either a new flight ID or a new pickup time must be provided");
        }

        String targetFlightId = (input.getNewFlightId() != null && !input.getNewFlightId().isBlank())
                ? input.getNewFlightId().trim()
                : booking.getFlightId();

        FlightDto flight = catalogCacheService.findFlight(targetFlightId)
                .orElseThrow(() -> new RuleViolationException("R1", "Flight '" + targetFlightId + "' not found in catalog"));

        HotelDto hotel = catalogCacheService.findHotel(booking.getHotelId())
                .orElseThrow(() -> new RuleViolationException("R1", "Hotel '" + booking.getHotelId() + "' not found in catalog"));

        AirportDto airport = catalogCacheService.findAirport(flight.getTo())
                .orElseThrow(() -> new RuleViolationException("R1", "Arrival airport '" + flight.getTo() + "' not found in catalog"));

        boolean servesCity = airport.getServesCities() != null &&
                airport.getServesCities().stream().anyMatch(city -> city.equalsIgnoreCase(hotel.getCity()));
        if (!servesCity) {
            throw new RuleViolationException("R1", String.format(
                    "New flight airport '%s' does not serve hotel city '%s'", flight.getTo(), hotel.getCity()));
        }

        LocalDateTime landingTime = flight.getArrivalLocal();
        LocalDateTime earliestPickup = landingTime.plusMinutes(60);
        LocalDateTime targetPickup = input.getNewPickupTime();

        if (targetPickup == null) {
            targetPickup = earliestPickup;
        } else if (targetPickup.isBefore(earliestPickup)) {
            throw new RuleViolationException("R2", String.format(
                    "New pickup time (%s) must be at least 60 minutes after flight landing (%s). Earliest pickup is %s",
                    targetPickup, landingTime, earliestPickup));
        }

        ChangeRequest changeRequest = ChangeRequest.builder()
                .booking(booking)
                .newFlightId(input.getNewFlightId())
                .newPickupTime(targetPickup)
                .status(ChangeRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return changeRequestRepository.save(changeRequest);
    }

    /**
     * Rules R9 & M7: Supplier resolves change request (ACCEPT or REJECT).
     */
    @Transactional
    public ChangeRequest resolveChangeRequest(Long changeRequestId, ResolveChangeRequestInput input) {
        log.info("Resolving change request ID: {} with action: {}", changeRequestId, input.getStatus());

        ChangeRequest changeRequest = changeRequestRepository.findById(changeRequestId)
                .orElseThrow(() -> new RuleViolationException("R9", "Change request not found with ID: " + changeRequestId));

        if (changeRequest.getStatus() != ChangeRequestStatus.PENDING) {
            throw new RuleViolationException("R9", "Change request is already resolved with status: " + changeRequest.getStatus());
        }

        String action = input.getStatus() != null ? input.getStatus().trim().toUpperCase() : "";

        if ("REJECTED".equals(action)) {
            changeRequest.setStatus(ChangeRequestStatus.REJECTED);
            changeRequest.setRejectionReason(input.getRejectionReason() != null ? input.getRejectionReason() : "Rejected by supplier");
            changeRequest.setResolvedAt(LocalDateTime.now());
            return changeRequestRepository.save(changeRequest);
        }

        if (!"ACCEPTED".equals(action)) {
            throw new RuleViolationException("R9", "Invalid action status: " + input.getStatus() + ". Must be ACCEPTED or REJECTED");
        }

        Booking booking = changeRequest.getBooking();
        String targetFlightId = changeRequest.getNewFlightId() != null && !changeRequest.getNewFlightId().isBlank()
                ? changeRequest.getNewFlightId()
                : booking.getFlightId();

        FlightDto flight = catalogCacheService.findFlight(targetFlightId)
                .orElseThrow(() -> new RuleViolationException("R1", "Flight '" + targetFlightId + "' not found"));
        HotelDto hotel = catalogCacheService.findHotel(booking.getHotelId())
                .orElseThrow(() -> new RuleViolationException("R1", "Hotel '" + booking.getHotelId() + "' not found"));
        RouteDto route = catalogCacheService.findRoute(flight.getTo(), hotel.getCity(), hotel.getArea())
                .orElseThrow(() -> new RuleViolationException("R3", "Route not found for flight arrival and hotel area"));

        LocalDateTime earliestPickup = flight.getArrivalLocal().plusMinutes(60);
        if (changeRequest.getNewPickupTime().isBefore(earliestPickup)) {
            throw new RuleViolationException("R2", String.format(
                    "New pickup time (%s) is before earliest allowed pickup (%s)",
                    changeRequest.getNewPickupTime(), earliestPickup));
        }

        LocalDateTime newBusyStart = changeRequest.getNewPickupTime();
        LocalDateTime newBusyEnd = newBusyStart.plusMinutes(2L * route.getDriveMinutes());

        // CRITICAL REQUIREMENT: Overlap query MUST exclude current bookingId
        long overlapCount = bookingRepository.countOverlappingBusyTimesExcludingBooking(
                booking.getCarId(), newBusyStart, newBusyEnd, booking.getId());

        if (overlapCount > 0) {
            throw new RuleViolationException("R9", String.format(
                    "Cannot accept change: Car '%s' is busy with another booking during new window [%s - %s]",
                    booking.getCarId(), newBusyStart, newBusyEnd));
        }

        booking.setFlightId(targetFlightId);
        booking.setPickupTime(changeRequest.getNewPickupTime());
        booking.setBusyStartTime(newBusyStart);
        booking.setBusyEndTime(newBusyEnd);
        bookingRepository.save(booking);

        changeRequest.setStatus(ChangeRequestStatus.ACCEPTED);
        changeRequest.setResolvedAt(LocalDateTime.now());
        return changeRequestRepository.save(changeRequest);
    }

    public Booking getBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new RuleViolationException("NOT_FOUND", "Booking not found with booking number: " + bookingNumber));
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuleViolationException("NOT_FOUND", "Booking not found with ID: " + id));
    }

    public RideRequest getRequestById(String requestId) {
        return rideRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuleViolationException("NOT_FOUND", "Ride request not found with ID: " + requestId));
    }

    public List<RideRequest> getAllOpenRequests() {
        return rideRequestRepository.findByStatus(RequestStatus.OPEN);
    }

    public List<RideOffer> getOffersForRequest(String requestId) {
        return rideOfferRepository.findByRideRequest_RequestId(requestId);
    }

    public List<RideOffer> getOffersForCompany(String companyId) {
        return rideOfferRepository.findByCompanyId(companyId);
    }

    public List<ChangeRequest> getChangeRequestsForBooking(Long bookingId) {
        return changeRequestRepository.findByBooking_Id(bookingId);
    }

    public List<ChangeRequest> getPendingChangeRequestsForCompany(String companyId) {
        return changeRequestRepository.findByBooking_CompanyIdAndStatus(companyId, ChangeRequestStatus.PENDING);
    }

    public List<SupplierEligibleRequestResponse> getEligibleRequestsForCompany(String companyId) {
        TaxiCompanyDto company = catalogCacheService.findCompany(companyId)
                .orElseThrow(() -> new RuleViolationException("R4", "Supplier company '" + companyId + "' not found"));

        List<RideRequest> openRequests = rideRequestRepository.findByStatus(RequestStatus.OPEN);
        List<SupplierEligibleRequestResponse> result = new ArrayList<>();

        for (RideRequest req : openRequests) {
            FlightDto flight = catalogCacheService.findFlight(req.getFlightId()).orElse(null);
            HotelDto hotel = catalogCacheService.findHotel(req.getHotelId()).orElse(null);
            if (flight == null || hotel == null) continue;

            RouteDto route = catalogCacheService.findRoute(flight.getTo(), hotel.getCity(), hotel.getArea()).orElse(null);
            if (route == null) continue;

            if (!company.servesAirport(flight.getTo())) continue;
            if (!company.servesArea(hotel.getCity(), hotel.getArea())) continue;
            if (!company.isOpenAt(req.getPickupTime().toLocalTime())) continue;

            LocalDateTime busyStart = req.getPickupTime();
            LocalDateTime busyEnd = busyStart.plusMinutes(2L * route.getDriveMinutes());
            int totalPassengers = req.getTotalPassengers();

            List<VehicleDto> matchingVehicles = new ArrayList<>();
            for (VehicleDto v : company.getVehicles()) {
                if (v.getSeats() < totalPassengers) continue;
                if (v.getLargeBags() < req.getLargeBags()) continue;
                if (v.getSmallBags() < req.getSmallBags()) continue;
                if (req.getCarType() != null && v.getType() != req.getCarType()) continue;

                long busyOverlap = bookingRepository.countOverlappingBusyTimes(v.getVehicleId(), busyStart, busyEnd);
                if (busyOverlap == 0) {
                    matchingVehicles.add(v);
                }
            }

            if (!matchingVehicles.isEmpty()) {
                result.add(SupplierEligibleRequestResponse.builder()
                        .request(dtoMapper.toResponse(req))
                        .eligibleVehicles(matchingVehicles)
                        .priceMinInr(route.getPriceMinInr())
                        .priceMaxInr(route.getPriceMaxInr())
                        .driveMinutes(route.getDriveMinutes())
                        .build());
            }
        }

        return result;
    }

    private String generateUnique10DigitBookingNumber() {
        for (int attempt = 0; attempt < 20; attempt++) {
            long num = 1_000_000_000L + (Math.abs(secureRandom.nextLong()) % 9_000_000_000L);
            String candidate = String.valueOf(num);
            if (bookingRepository.findByBookingNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        return String.valueOf(System.currentTimeMillis()).substring(3, 13);
    }
}
