package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByBloodGroup(String bloodGroup);
    List<BloodRequest> findByBloodGroupAndStatus(String bloodGroup, String status);
    List<BloodRequest> findByHospital(String hospital);
    List<BloodRequest> findByHospitalAndStatus(String hospital, String status);
    List<BloodRequest> findByAcceptedById(Long patientId);
}
