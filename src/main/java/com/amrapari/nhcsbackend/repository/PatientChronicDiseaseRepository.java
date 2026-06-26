package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.PatientChronicDisease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientChronicDiseaseRepository extends JpaRepository<PatientChronicDisease, Long> {
}
