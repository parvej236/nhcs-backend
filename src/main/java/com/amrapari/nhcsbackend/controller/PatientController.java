package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.Patient;
import com.amrapari.nhcsbackend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.amrapari.nhcsbackend.repository.UserRepository;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Patient> getMyProfile(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .map(user -> {
                    Patient patient = patientRepository.findByUserId(user.getId()).orElseGet(() -> {
                        Patient newPatient = new Patient();
                        newPatient.setUser(user);
                        newPatient.setFullName(user.getUsername());
                        return patientRepository.save(newPatient);
                    });
                    return ResponseEntity.ok(patient);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Patient> updateMyProfile(Authentication authentication, @RequestBody Patient patientDetails) {
        return userRepository.findByUsername(authentication.getName())
                .map(user -> {
                    Patient patient = patientRepository.findByUserId(user.getId()).orElseGet(() -> {
                        Patient newPatient = new Patient();
                        newPatient.setUser(user);
                        return newPatient;
                    });
                    if (patientDetails.getFullName() != null) patient.setFullName(patientDetails.getFullName());
                    if (patientDetails.getDateOfBirth() != null) patient.setDateOfBirth(patientDetails.getDateOfBirth());
                    if (patientDetails.getGender() != null) patient.setGender(patientDetails.getGender());
                    if (patientDetails.getBloodGroup() != null) patient.setBloodGroup(patientDetails.getBloodGroup());
                    if (patientDetails.getContactNumber() != null) patient.setContactNumber(patientDetails.getContactNumber());
                    if (patientDetails.getAddress() != null) patient.setAddress(patientDetails.getAddress());
                    if (patientDetails.getOccupation() != null) patient.setOccupation(patientDetails.getOccupation());
                    if (patientDetails.getMaritalStatus() != null) patient.setMaritalStatus(patientDetails.getMaritalStatus());
                    if (patientDetails.getPresentAddress() != null) patient.setPresentAddress(patientDetails.getPresentAddress());
                    if (patientDetails.getPermanentAddress() != null) patient.setPermanentAddress(patientDetails.getPermanentAddress());
                    if (patientDetails.getEmergencyContactName() != null) patient.setEmergencyContactName(patientDetails.getEmergencyContactName());
                    if (patientDetails.getEmergencyContactRelation() != null) patient.setEmergencyContactRelation(patientDetails.getEmergencyContactRelation());
                    if (patientDetails.getEmergencyContactPhone() != null) patient.setEmergencyContactPhone(patientDetails.getEmergencyContactPhone());
                    return ResponseEntity.ok(patientRepository.save(patient));
                }).orElse(ResponseEntity.notFound().build());
    }
}
