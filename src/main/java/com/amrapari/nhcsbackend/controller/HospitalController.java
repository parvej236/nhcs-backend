package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.Hospital;
import com.amrapari.nhcsbackend.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {
    private final HospitalRepository hospitalRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('GOVT', 'ADMIN', 'PATIENT', 'DOCTOR', 'HOSPITAL')")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GOVT', 'ADMIN')")
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
        // Auto-generate Facility ID if missing
        if (hospital.getFacilityId() == null || hospital.getFacilityId().isEmpty()) {
            hospital.setFacilityId("FAC-" + (int)(Math.random() * 10000));
        }
        if (hospital.getStatus() == null || hospital.getStatus().isEmpty()) {
            hospital.setStatus("Active");
        }
        if (hospital.getComplianceScore() == null) {
            hospital.setComplianceScore(100);
        }
        if (hospital.getOccupiedBeds() == null) {
            hospital.setOccupiedBeds(0);
        }
        if (hospital.getTotalBeds() == null) {
            hospital.setTotalBeds(0);
        }
        return ResponseEntity.ok(hospitalRepository.save(hospital));
    }
}
