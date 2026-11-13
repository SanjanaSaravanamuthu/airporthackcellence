package com.airporttaxi.dto.mapper;

import com.airporttaxi.domain.entity.Booking;
import com.airporttaxi.domain.entity.ChangeRequest;
import com.airporttaxi.domain.entity.RideOffer;
import com.airporttaxi.domain.entity.RideRequest;
import com.airporttaxi.dto.catalog.TaxiCompanyDto;
import com.airporttaxi.dto.catalog.VehicleDto;
import com.airporttaxi.dto.response.BookingResponse;
import com.airporttaxi.dto.response.ChangeRequestResponse;
import com.airporttaxi.dto.response.RideOfferResponse;
import com.airporttaxi.dto.response.RideRequestResponse;
import com.airporttaxi.service.CatalogCacheService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DtoMapper {

    private final CatalogCacheService catalogCacheService;

    public DtoMapper(CatalogCacheService catalogCacheService) {
        this.catalogCacheService = catalogCacheService;
    }

    public RideRequestResponse toResponse(RideRequest entity) {
        if (entity == null) return null;
        return RideRequestResponse.builder()
                .requestId(entity.getRequestId())
                .travelerName(entity.getTravelerName())
                .flightId(entity.getFlightId())
                .hotelId(entity.getHotelId())
                .adults(entity.getAdults())
                .childAges(entity.getChildAges())
                .largeBags(entity.getLargeBags())
                .smallBags(entity.getSmallBags())
                .pickupTime(entity.getPickupTime())
                .carType(entity.getCarType())
                .priceInr(entity.getPriceInr())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public RideOfferResponse toResponse(RideOffer entity) {
        if (entity == null) return null;

        String companyName = catalogCacheService.findCompany(entity.getCompanyId())
                .map(TaxiCompanyDto::getName)
                .orElse(entity.getCompanyId());

        Optional<VehicleDto> vehicleOpt = catalogCacheService.findVehicle(entity.getVehicleId());

        return RideOfferResponse.builder()
                .id(entity.getId())
                .requestId(entity.getRideRequest() != null ? entity.getRideRequest().getRequestId() : null)
                .companyId(entity.getCompanyId())
                .companyName(companyName)
                .vehicleId(entity.getVehicleId())
                .vehicleType(vehicleOpt.map(VehicleDto::getType).orElse(null))
                .seats(vehicleOpt.map(VehicleDto::getSeats).orElse(0))
                .largeBags(vehicleOpt.map(VehicleDto::getLargeBags).orElse(0))
                .smallBags(vehicleOpt.map(VehicleDto::getSmallBags).orElse(0))
                .offeredPriceInr(entity.getOfferedPriceInr())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public BookingResponse toResponse(Booking entity) {
        if (entity == null) return null;

        String companyName = catalogCacheService.findCompany(entity.getCompanyId())
                .map(TaxiCompanyDto::getName)
                .orElse(entity.getCompanyId());

        return BookingResponse.builder()
                .id(entity.getId())
                .bookingNumber(entity.getBookingNumber())
                .requestId(entity.getRequestId())
                .companyId(entity.getCompanyId())
                .companyName(companyName)
                .carId(entity.getCarId())
                .flightId(entity.getFlightId())
                .hotelId(entity.getHotelId())
                .pickupTime(entity.getPickupTime())
                .busyStartTime(entity.getBusyStartTime())
                .busyEndTime(entity.getBusyEndTime())
                .priceInr(entity.getPriceInr())
                .status(entity.getStatus())
                .isPreBooked(entity.isPreBooked())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ChangeRequestResponse toResponse(ChangeRequest entity) {
        if (entity == null) return null;

        return ChangeRequestResponse.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .bookingNumber(entity.getBooking() != null ? entity.getBooking().getBookingNumber() : null)
                .newFlightId(entity.getNewFlightId())
                .newPickupTime(entity.getNewPickupTime())
                .status(entity.getStatus())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
