package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/blood-donations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class BloodDonationController {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final BloodDonorRepository bloodDonorRepository;
    private final BloodRequestRepository bloodRequestRepository;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    Optional<BloodDonor> donorOpt = bloodDonorRepository.findByPatientId(patient.getId());
                    boolean active = donorOpt.map(BloodDonor::isActive).orElse(false);
                    LocalDate lastDonation = donorOpt.map(BloodDonor::getLastDonationDate).orElse(null);
                    
                    String bloodGroup = patient.getBloodGroup();
                    if (bloodGroup == null || bloodGroup.trim().isEmpty()) {
                        bloodGroup = "O+";
                    }

                    // AI Eligibility simulation determination
                    String eligibility = "safe";
                    boolean hasAsthma = patient.getChronicDiseases().stream()
                            .anyMatch(cd -> cd.getDiseaseName().toLowerCase().contains("asthma"));
                    if (hasAsthma) {
                        eligibility = "asthma";
                    } else if (lastDonation != null && lastDonation.isAfter(LocalDate.now().minusMonths(3))) {
                        eligibility = "recent";
                    }

                    List<BloodRequest> matchingRequests = bloodRequestRepository.findByBloodGroupAndStatus(bloodGroup, "Pending");

                    Map<String, Object> response = new HashMap<>();
                    response.put("active", active);
                    response.put("bloodGroup", bloodGroup);
                    response.put("lastDonationDate", lastDonation != null ? lastDonation.toString() : null);
                    response.put("eligibilitySim", eligibility);
                    response.put("requests", matchingRequests.stream().map(req -> {
                        Map<String, Object> r = new HashMap<>();
                        r.put("id", req.getId());
                        r.put("patientName", req.getPatientName());
                        r.put("bloodGroup", req.getBloodGroup());
                        r.put("urgency", req.getUrgency());
                        r.put("hospital", req.getHospital());
                        r.put("location", req.getLocation());
                        r.put("timeline", req.getTimeline());
                        r.put("status", req.getStatus());
                        return r;
                    }).collect(Collectors.toList()));

                    return ResponseEntity.ok(response);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/toggle-donor")
    public ResponseEntity<?> toggleDonor(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    BloodDonor donor = bloodDonorRepository.findByPatientId(patient.getId())
                            .orElseGet(() -> {
                                String bg = patient.getBloodGroup();
                                if (bg == null || bg.trim().isEmpty()) {
                                    bg = "O+";
                                }
                                return BloodDonor.builder()
                                        .patient(patient)
                                        .bloodGroup(bg)
                                        .active(false)
                                        .build();
                            });
                    donor.setActive(!donor.isActive());
                    bloodDonorRepository.save(donor);

                    Map<String, Object> response = new HashMap<>();
                    response.put("active", donor.isActive());
                    response.put("bloodGroup", donor.getBloodGroup());
                    response.put("lastDonationDate", donor.getLastDonationDate() != null ? donor.getLastDonationDate().toString() : null);
                    return ResponseEntity.ok(response);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(Authentication authentication, @PathVariable Long requestId) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(requestId);
                    if (reqOpt.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    BloodRequest req = reqOpt.get();
                    req.setStatus("Accepted");
                    req.setAcceptedBy(patient);
                    bloodRequestRepository.save(req);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("status", "Accepted");
                    return ResponseEntity.ok(response);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/requests/{requestId}/decline")
    public ResponseEntity<?> declineRequest(Authentication authentication, @PathVariable Long requestId) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(requestId);
                    if (reqOpt.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }
                    BloodRequest req = reqOpt.get();
                    req.setStatus("Declined");
                    req.setAcceptedBy(patient);
                    bloodRequestRepository.save(req);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("status", "Declined");
                    return ResponseEntity.ok(response);
                }).orElse(ResponseEntity.notFound().build());
    }
}
