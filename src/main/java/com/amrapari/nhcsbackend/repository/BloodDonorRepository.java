package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.BloodDonor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BloodDonorRepository extends JpaRepository<BloodDonor, Long> {
    Optional<BloodDonor> findByPatientId(Long patientId);
}
