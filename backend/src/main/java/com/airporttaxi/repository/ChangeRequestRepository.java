package com.airporttaxi.repository;

import com.airporttaxi.domain.entity.ChangeRequest;
import com.airporttaxi.domain.enums.ChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {

    List<ChangeRequest> findByBooking_Id(Long bookingId);

    List<ChangeRequest> findByBooking_CompanyIdAndStatus(String companyId, ChangeRequestStatus status);
}

