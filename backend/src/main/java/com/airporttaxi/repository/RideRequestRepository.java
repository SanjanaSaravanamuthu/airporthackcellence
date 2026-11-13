package com.airporttaxi.repository;

import com.airporttaxi.domain.entity.RideRequest;
import com.airporttaxi.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, String> {

    List<RideRequest> findByStatus(RequestStatus status);

    List<RideRequest> findAllByOrderByCreatedAtDesc();
}

