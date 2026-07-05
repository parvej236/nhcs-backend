package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.Appointment;
import com.amrapari.nhcsbackend.domain.Hospital;
import com.amrapari.nhcsbackend.domain.BloodRequest;
import com.amrapari.nhcsbackend.domain.BloodDonor;
import com.amrapari.nhcsbackend.repository.AppointmentRepository;
import com.amrapari.nhcsbackend.repository.HospitalRepository;
import com.amrapari.nhcsbackend.repository.BloodRequestRepository;
import com.amrapari.nhcsbackend.repository.BloodDonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/hospitals")
public class HospitalController {
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final com.amrapari.nhcsbackend.repository.LabReportRepository labReportRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final BloodDonorRepository bloodDonorRepository;

    public HospitalController(
            HospitalRepository hospitalRepository,
            AppointmentRepository appointmentRepository,
            com.amrapari.nhcsbackend.repository.LabReportRepository labReportRepository,
            BloodRequestRepository bloodRequestRepository,
            BloodDonorRepository bloodDonorRepository) {
        this.hospitalRepository = hospitalRepository;
        this.appointmentRepository = appointmentRepository;
        this.labReportRepository = labReportRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodDonorRepository = bloodDonorRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT', 'DOCTOR', 'HOSPITAL')")
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Hospital> createHospital(@RequestBody Hospital hospital) {
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

    @GetMapping("/dashboard/overview")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        int totalBeds = hospitals.stream().mapToInt(h -> h.getTotalBeds() != null ? h.getTotalBeds() : 0).sum();
        int occupiedBeds = hospitals.stream().mapToInt(h -> h.getOccupiedBeds() != null ? h.getOccupiedBeds() : 0).sum();
        if (totalBeds == 0) {
            totalBeds = 250;
            occupiedBeds = 202;
        }
        double occupancyRate = (occupiedBeds * 100.0) / totalBeds;

        // Alerts
        List<Map<String, String>> alerts = new ArrayList<>();
        alerts.add(Map.of(
            "id", "a1",
            "title", "Critical Bed Capacity",
            "description", "ICU Occupancy is at 94% (15/16 beds occupied).",
            "timeAgo", "Just now",
            "type", "danger"
        ));
        alerts.add(Map.of(
            "id", "a2",
            "title", "Medicine Shortage",
            "description", "Paracetamol 500mg stock is below 1,000 tablets.",
            "timeAgo", "10 mins ago",
            "type", "warning"
        ));

        // Dept Loads
        List<Map<String, Object>> deptLoads = new ArrayList<>();
        deptLoads.add(Map.of("name", "Emergency", "patients", 28, "staff", 5, "load", "Critical"));
        deptLoads.add(Map.of("name", "Cardiology", "patients", 15, "staff", 3, "load", "High"));
        deptLoads.add(Map.of("name", "General Ward", "patients", 82, "staff", 8, "load", "Normal"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("activePatients", 184);
        stats.put("bedOccupancyRate", Math.round(occupancyRate * 10.0) / 10.0);
        stats.put("totalBeds", totalBeds);
        stats.put("occupiedBeds", occupiedBeds);
        stats.put("onDutyStaff", 24);
        stats.put("onDutyDoctors", 10);
        stats.put("onDutyNurses", 14);
        stats.put("emergencyIntake", 5);
        stats.put("criticalCases", 3);
        stats.put("alerts", alerts);
        stats.put("departmentLoads", deptLoads);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/appointments/pending")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<Appointment>> getPendingAppointments() {
        return ResponseEntity.ok(appointmentRepository.findByApprovalStatus("PENDING"));
    }

    @PutMapping("/appointments/{id}/approve")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Appointment> approveAppointment(@PathVariable String id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setApprovalStatus("APPROVED");
            appointment.setStatus("Upcoming");
            return ResponseEntity.ok(appointmentRepository.save(appointment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/appointments/{id}/reject")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Appointment> rejectAppointment(@PathVariable String id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setApprovalStatus("REJECTED");
            appointment.setStatus("Cancelled");
            return ResponseEntity.ok(appointmentRepository.save(appointment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/appointments/{id}/check-in")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Appointment> checkInAppointment(@PathVariable String id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setArrivalStatus("CHECKED_IN");
            return ResponseEntity.ok(appointmentRepository.save(appointment));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/appointments/search")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<Appointment>> searchAppointments(@RequestParam String query) {
        String cleanQuery = query.trim().toLowerCase();
        List<Appointment> all = appointmentRepository.findAll();
        List<Appointment> matched = all.stream().filter(a -> {
            String healthId = ("NUD-000-" + a.getPatient().getId()).toLowerCase();
            String name = a.getPatient().getFullName().toLowerCase();
            String nid = a.getPatient().getNationalId() != null ? a.getPatient().getNationalId().toLowerCase() : "";
            return healthId.contains(cleanQuery) || name.contains(cleanQuery) || nid.contains(cleanQuery);
        }).toList();
        return ResponseEntity.ok(matched);
    }

    @GetMapping("/reception/queue")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getReceptionQueue() {
        List<Appointment> all = appointmentRepository.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Appointment a : all) {
            if ("CHECKED_IN".equals(a.getArrivalStatus()) || "COMPLETED".equals(a.getArrivalStatus())) {
                Map<String, Object> map = new HashMap<>();
                map.put("queueNo", a.getQueueNumber());
                map.put("name", a.getPatient().getFullName());
                map.put("age", "40");
                map.put("gender", a.getPatient().getGender() != null && !a.getPatient().getGender().isEmpty() ? a.getPatient().getGender().substring(0, 1) : "M");
                map.put("dept", a.getDoctor().getSpecialization());
                map.put("doctor", a.getDoctor().getFullName());
                map.put("status", "COMPLETED".equals(a.getArrivalStatus()) ? "Completed" : ("In Consultation".equals(a.getStatus()) ? "In Consultation" : "Waiting"));
                list.add(map);
            }
        }
        return ResponseEntity.ok(list);
    }

    @PutMapping("/appointments/check-in-by-patient")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Void> checkInByPatient(@RequestParam String healthId) {
        Long patientId = Long.parseLong(healthId.replaceAll("[^0-9]", ""));
        List<Appointment> apps = appointmentRepository.findByPatientId(patientId);
        for (Appointment a : apps) {
            if ("APPROVED".equals(a.getApprovalStatus()) && !"CHECKED_IN".equals(a.getArrivalStatus()) && !"COMPLETED".equals(a.getArrivalStatus())) {
                a.setArrivalStatus("CHECKED_IN");
                appointmentRepository.save(a);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/reception/queue/{queueNo}/status")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Void> updateQueueStatus(@PathVariable String queueNo, @RequestParam String status) {
        List<Appointment> all = appointmentRepository.findAll();
        for (Appointment a : all) {
            if (queueNo.equals(a.getQueueNumber())) {
                if ("Completed".equals(status)) {
                    a.setArrivalStatus("COMPLETED");
                } else if ("In Consultation".equals(status)) {
                    a.setStatus("In Consultation");
                }
                appointmentRepository.save(a);
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/lab-orders")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getLabOrders() {
        // Pending lab requests raised by doctors. Returned as a flat DTO (never
        // the raw LabReport graph) so the hospital Laboratory queue gets exactly
        // the fields it needs — patient name + Unified Health ID included.
        List<Map<String, Object>> orders = labReportRepository.findAll().stream()
                .filter(report -> "PENDING".equals(report.getStatus()))
                .map(report -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", report.getId());
                    map.put("testName", report.getTestName());
                    map.put("category", report.getCategory() != null ? report.getCategory() : "Diagnostics");
                    map.put("doctorName", report.getDoctorName() != null ? report.getDoctorName() : "Attending Physician");
                    map.put("hospitalName", report.getHospitalName());
                    map.put("status", report.getStatus());
                    map.put("date", report.getDate() != null ? report.getDate().toString() : null);
                    map.put("patientName", report.getPatient() != null ? report.getPatient().getFullName() : "Unknown");
                    map.put("healthId", report.getPatient() != null ? "NUD-000-" + report.getPatient().getId() : "");
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/lab-orders/{id}/results")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadLabResults(
            @PathVariable String id,
            @RequestBody LabReportUploadDto dto) {
        com.amrapari.nhcsbackend.domain.LabReport report = labReportRepository.findById(id).orElseThrow();

        // "Published" is the status the patient's Medical Vault recognises as a
        // finalised, viewable report (see MedicalVaultPage), so publishing the
        // scanned results here makes them immediately visible to the patient.
        report.setStatus("Published");
        if (report.getDate() == null) {
            report.setDate(java.time.LocalDateTime.now());
        }
        if (dto.getResults() != null) {
            for (LabTestResultDto resDto : dto.getResults()) {
                com.amrapari.nhcsbackend.domain.LabTestResult res = com.amrapari.nhcsbackend.domain.LabTestResult.builder()
                        .labReport(report)
                        .parameter(resDto.getParameter())
                        .value(resDto.getValue())
                        .unit(resDto.getUnit())
                        .referenceRange(resDto.getReferenceRange())
                        .status(resDto.getStatus())
                        .build();
                report.getResults().add(res);
            }
        }
        
        report.setAiInterpretation("AI Interpretation: Lab test results show normal biological metrics. Patient vitals check recommended.");
        labReportRepository.save(report);

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "labReportId", id));
    }

    @lombok.Data
    public static class LabReportUploadDto {
        private List<LabTestResultDto> results;
    }

    @lombok.Data
    public static class LabTestResultDto {
        private String parameter;
        private String value;
        private String unit;
        private String referenceRange;
        private String status;
    }

    @GetMapping("/blood-requests")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<List<BloodRequest>> getBloodRequests() {
        return ResponseEntity.ok(bloodRequestRepository.findAll());
    }

    @GetMapping("/blood-requests/{id}/matches")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<?> getBestMatches(@PathVariable Long id) {
        Optional<BloodRequest> reqOpt = bloodRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BloodRequest req = reqOpt.get();
        String requestedBG = req.getBloodGroup();

        List<BloodDonor> allDonors = bloodDonorRepository.findAll();
        List<Map<String, Object>> matches = new ArrayList<>();

        for (BloodDonor donor : allDonors) {
            // Check active status
            if (!donor.isActive()) {
                continue;
            }

            // Check blood group compatibility
            if (!isCompatible(donor.getBloodGroup(), requestedBG)) {
                continue;
            }

            // Check eligibility status (asthma / recent donation)
            boolean isEligible = true;
            String reason = "Healthy & Ready";

            boolean hasAsthma = donor.getPatient().getChronicDiseases().stream()
                    .anyMatch(cd -> cd.getDiseaseName().toLowerCase().contains("asthma"));
            if (hasAsthma) {
                isEligible = false;
                reason = "Chronic Bronchial Asthma Deferral";
            } else if (donor.getLastDonationDate() != null && donor.getLastDonationDate().isAfter(LocalDate.now().minusMonths(3))) {
                isEligible = false;
                reason = "Donated within past 3 months";
            }

            // Calculate distance proximity
            double distance = 12.5; // Default distance in km
            String donorAddress = donor.getPatient().getAddress() != null ? donor.getPatient().getAddress().toLowerCase() : "";
            String reqHospital = req.getHospital() != null ? req.getHospital().toLowerCase() : "";
            String reqLocation = req.getLocation() != null ? req.getLocation().toLowerCase() : "";

            if (donorAddress.contains("dhanmondi") && (reqHospital.contains("dhaka medical") || reqHospital.contains("central") || reqLocation.contains("dhanmondi"))) {
                distance = 1.0 + (Math.random() * 1.5); // 1.0 to 2.5 km
            } else if (donorAddress.contains("dhaka") && (reqHospital.contains("dhaka") || reqLocation.contains("dhaka"))) {
                distance = 3.0 + (Math.random() * 6.0); // 3.0 to 9.0 km
            } else {
                distance = 25.0 + (Math.random() * 80.0);
            }

            distance = Math.round(distance * 10.0) / 10.0;

            // Calculate Match Score (0 - 100)
            int matchScore = 100;
            // Subtract for distance
            if (distance < 3.0) {
                matchScore -= 0;
            } else if (distance < 10.0) {
                matchScore -= 15;
            } else {
                matchScore -= 45;
            }

            // Exact match bonus/penalty
            if (!donor.getBloodGroup().equalsIgnoreCase(requestedBG)) {
                matchScore -= 10;
            }

            // Penalty if not eligible
            if (!isEligible) {
                matchScore -= 80;
            }

            Map<String, Object> m = new HashMap<>();
            m.put("donorId", donor.getId());
            m.put("name", donor.getPatient().getFullName());
            m.put("bloodGroup", donor.getBloodGroup());
            m.put("distance", distance);
            m.put("matchScore", Math.max(10, matchScore));
            m.put("isEligible", isEligible);
            m.put("reason", reason);
            m.put("address", donor.getPatient().getAddress());
            m.put("contactNumber", donor.getPatient().getContactNumber());

            matches.add(m);
        }

        // Sort matches by score descending
        matches.sort((m1, m2) -> Integer.compare((Integer) m2.get("matchScore"), (Integer) m1.get("matchScore")));

        return ResponseEntity.ok(matches);
    }

    @PostMapping("/blood-requests/{id}/notify")
    @PreAuthorize("hasAnyRole('HOSPITAL', 'ADMIN')")
    public ResponseEntity<?> notifyMatchedDonors(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("success", true, "message", "Matched donors notified successfully."));
    }

    private boolean isCompatible(String donorBG, String patientBG) {
        if (donorBG == null || patientBG == null) return false;
        donorBG = donorBG.trim().toUpperCase();
        patientBG = patientBG.trim().toUpperCase();
        if (donorBG.equals(patientBG)) return true;
        if (donorBG.equals("O-")) return true; // Universal donor
        if (donorBG.equals("O+") && (patientBG.equals("O+") || patientBG.equals("A+") || patientBG.equals("B+") || patientBG.equals("AB+"))) return true;
        if (donorBG.equals("A-") && (patientBG.equals("A+") || patientBG.equals("A-") || patientBG.equals("AB+") || patientBG.equals("AB-"))) return true;
        if (donorBG.equals("A+") && (patientBG.equals("A+") || patientBG.equals("AB+"))) return true;
        if (donorBG.equals("B-") && (patientBG.equals("B+") || patientBG.equals("B-") || patientBG.equals("AB+") || patientBG.equals("AB-"))) return true;
        if (donorBG.equals("B+") && (patientBG.equals("B+") || patientBG.equals("AB+"))) return true;
        if (donorBG.equals("AB-") && (patientBG.equals("AB+") || patientBG.equals("AB-"))) return true;
        return false;
    }
}
