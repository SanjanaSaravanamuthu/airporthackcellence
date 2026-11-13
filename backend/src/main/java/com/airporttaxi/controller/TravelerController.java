package com.airporttaxi.controller;

import com.airporttaxi.domain.entity.Booking;
import com.airporttaxi.domain.entity.RideOffer;
import com.airporttaxi.domain.entity.RideRequest;
import com.airporttaxi.dto.mapper.DtoMapper;
import com.airporttaxi.dto.request.TravelerRideRequestInput;
import com.airporttaxi.dto.response.BookingResponse;
import com.airporttaxi.dto.response.RideOfferResponse;
import com.airporttaxi.dto.response.RideRequestResponse;
import com.airporttaxi.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/traveler")
@CrossOrigin(origins = "*")
public class TravelerController {

    private final RideService rideService;
    private final DtoMapper dtoMapper;

    public TravelerController(RideService rideService, DtoMapper dtoMapper) {
        this.rideService = rideService;
        this.dtoMapper = dtoMapper;
    }

    @PostMapping("/requests")
    public ResponseEntity<RideRequestResponse> createRideRequest(@Valid @RequestBody TravelerRideRequestInput input) {
        RideRequest request = rideService.createRideRequest(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toResponse(request));
    }

    @GetMapping("/requests/{requestId}")
    public ResponseEntity<RideRequestResponse> getRideRequest(@PathVariable String requestId) {
        RideRequest request = rideService.getRequestById(requestId);
        return ResponseEntity.ok(dtoMapper.toResponse(request));
    }

    @GetMapping("/requests/{requestId}/offers")
    public ResponseEntity<List<RideOfferResponse>> getOffersForRequest(@PathVariable String requestId) {
        List<RideOffer> offers = rideService.getOffersForRequest(requestId);
        List<RideOfferResponse> responseList = offers.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/offers/{offerId}/book")
    public ResponseEntity<BookingResponse> bookOffer(@PathVariable Long offerId) {
        Booking booking = rideService.confirmBooking(offerId);
        return ResponseEntity.ok(dtoMapper.toResponse(booking));
    }

    @PostMapping("/offers/{offerId}/reject")
    public ResponseEntity<RideOfferResponse> rejectOffer(@PathVariable Long offerId) {
        RideOffer offer = rideService.rejectOffer(offerId);
        return ResponseEntity.ok(dtoMapper.toResponse(offer));
    }
}
