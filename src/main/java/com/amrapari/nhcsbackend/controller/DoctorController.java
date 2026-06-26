package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.Doctor;
import com.amrapari.nhcsbackend.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        return doctorRepository.findById(id).map(doctor -> {
            doctor.setFullName(doctorDetails.getFullName());
            doctor.setSpecialization(doctorDetails.getSpecialization());
            doctor.setLicenseNumber(doctorDetails.getLicenseNumber());
            doctor.setContactNumber(doctorDetails.getContactNumber());
            doctor.setHospitalAffiliation(doctorDetails.getHospitalAffiliation());
            return ResponseEntity.ok(doctorRepository.save(doctor));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<Map<String, Object>>> getAvailableSlots(@PathVariable Long id, @RequestParam String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        slots.add(Map.of("id", "TS-1", "time", "09:00 AM", "isAvailable", true));
        slots.add(Map.of("id", "TS-2", "time", "09:30 AM", "isAvailable", false));
        slots.add(Map.of("id", "TS-3", "time", "10:00 AM", "isAvailable", true));
        slots.add(Map.of("id", "TS-4", "time", "10:30 AM", "isAvailable", true));
        slots.add(Map.of("id", "TS-5", "time", "11:00 AM", "isAvailable", false));
        slots.add(Map.of("id", "TS-6", "time", "11:30 AM", "isAvailable", true));
        slots.add(Map.of("id", "TS-7", "time", "04:00 PM", "isAvailable", true));
        slots.add(Map.of("id", "TS-8", "time", "04:30 PM", "isAvailable", true));
        return ResponseEntity.ok(slots);
    }
}

