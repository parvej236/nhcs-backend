package com.amrapari.nhcsbackend.repository;

import com.amrapari.nhcsbackend.domain.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, Long> {
}
