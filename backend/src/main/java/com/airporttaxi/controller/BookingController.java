package com.airporttaxi.controller;

import com.airporttaxi.domain.entity.Booking;
import com.airporttaxi.domain.entity.ChangeRequest;
import com.airporttaxi.dto.mapper.DtoMapper;
import com.airporttaxi.dto.request.ChangeRequestInput;
import com.airporttaxi.dto.request.ResolveChangeRequestInput;
import com.airporttaxi.dto.response.BookingResponse;
import com.airporttaxi.dto.response.ChangeRequestResponse;
import com.airporttaxi.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final RideService rideService;
    private final DtoMapper dtoMapper;

    public BookingController(RideService rideService, DtoMapper dtoMapper) {
        this.rideService = rideService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/by-number/{bookingNumber}")
    public ResponseEntity<BookingResponse> getBookingByNumber(@PathVariable String bookingNumber) {
        Booking booking = rideService.getBookingByNumber(bookingNumber);
        return ResponseEntity.ok(dtoMapper.toResponse(booking));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        Booking booking = rideService.getBookingById(bookingId);
        return ResponseEntity.ok(dtoMapper.toResponse(booking));
    }

    @PostMapping("/{bookingId}/change-requests")
    public ResponseEntity<ChangeRequestResponse> submitChangeRequest(
            @PathVariable Long bookingId,
            @Valid @RequestBody ChangeRequestInput input) {
        ChangeRequest changeRequest = rideService.submitChangeRequest(bookingId, input);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toResponse(changeRequest));
    }

    @PatchMapping("/change-requests/{changeRequestId}")
    public ResponseEntity<ChangeRequestResponse> resolveChangeRequest(
            @PathVariable Long changeRequestId,
            @Valid @RequestBody ResolveChangeRequestInput input) {
        ChangeRequest changeRequest = rideService.resolveChangeRequest(changeRequestId, input);
        return ResponseEntity.ok(dtoMapper.toResponse(changeRequest));
    }

    @GetMapping("/{bookingId}/change-requests")
    public ResponseEntity<List<ChangeRequestResponse>> getChangeRequestsForBooking(@PathVariable Long bookingId) {
        List<ChangeRequest> list = rideService.getChangeRequestsForBooking(bookingId);
        List<ChangeRequestResponse> responseList = list.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }
}
