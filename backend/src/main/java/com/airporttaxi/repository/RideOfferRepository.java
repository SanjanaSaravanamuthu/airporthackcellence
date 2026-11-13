package com.airporttaxi.repository;

import com.airporttaxi.domain.entity.RideOffer;
import com.airporttaxi.domain.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideOfferRepository extends JpaRepository<RideOffer, Long> {

    List<RideOffer> findByRideRequest_RequestId(String requestId);

    List<RideOffer> findByCompanyId(String companyId);

    List<RideOffer> findByRideRequest_RequestIdAndCompanyId(String requestId, String companyId);

    @Modifying
    @Query("""
        UPDATE RideOffer o
        SET o.status = :status
        WHERE o.rideRequest.requestId = :requestId
          AND o.id <> :chosenOfferId
          AND o.status = com.airporttaxi.domain.enums.OfferStatus.OPEN
    """)
    int markOtherOffersStatus(
        @Param("requestId") String requestId,
        @Param("chosenOfferId") Long chosenOfferId,
        @Param("status") OfferStatus status
    );
}

