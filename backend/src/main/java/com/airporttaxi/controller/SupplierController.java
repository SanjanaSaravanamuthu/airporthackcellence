package com.airporttaxi.controller;

import com.airporttaxi.domain.entity.ChangeRequest;
import com.airporttaxi.domain.entity.RideOffer;
import com.airporttaxi.dto.mapper.DtoMapper;
import com.airporttaxi.dto.request.SupplierOfferInput;
import com.airporttaxi.dto.response.ChangeRequestResponse;
import com.airporttaxi.dto.response.RideOfferResponse;
import com.airporttaxi.dto.response.SupplierEligibleRequestResponse;
import com.airporttaxi.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final RideService rideService;
    private final DtoMapper dtoMapper;

    public SupplierController(RideService rideService, DtoMapper dtoMapper) {
        this.rideService = rideService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping("/{companyId}/open-requests")
    public ResponseEntity<List<SupplierEligibleRequestResponse>> getEligibleRequests(@PathVariable String companyId) {
        List<SupplierEligibleRequestResponse> eligible = rideService.getEligibleRequestsForCompany(companyId);
        return ResponseEntity.ok(eligible);
    }

    @PostMapping("/offers")
    public ResponseEntity<RideOfferResponse> submitOffer(@Valid @RequestBody SupplierOfferInput input) {
        RideOffer offer = rideService.submitRideOffer(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toResponse(offer));
    }

    @GetMapping("/{companyId}/offers")
    public ResponseEntity<List<RideOfferResponse>> getOffersByCompany(@PathVariable String companyId) {
        List<RideOffer> offers = rideService.getOffersForCompany(companyId);
        List<RideOfferResponse> responseList = offers.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{companyId}/change-requests")
    public ResponseEntity<List<ChangeRequestResponse>> getPendingChangeRequests(@PathVariable String companyId) {
        List<ChangeRequest> changeRequests = rideService.getPendingChangeRequestsForCompany(companyId);
        List<ChangeRequestResponse> responseList = changeRequests.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }
}
