package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByPatientIdOrderByDateDesc(Long patientId);
}
