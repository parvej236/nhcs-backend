package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientProfileDto> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(patient -> ResponseEntity.ok(mapToProfileDto(patient)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileDto> getMyProfile(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> ResponseEntity.ok(mapToProfileDto(patient)))
                .orElseGet(() -> {
                    // Create if not exists for the authenticated user
                    return userRepository.findByUsername(authentication.getName()).map(user -> {
                        Patient newPatient = new Patient();
                        newPatient.setUser(user);
                        newPatient.setFullName(user.getUsername());
                        newPatient.setVitalsLastUpdated(LocalDateTime.now());
                        Patient saved = patientRepository.save(newPatient);
                        return ResponseEntity.ok(mapToProfileDto(saved));
                    }).orElse(ResponseEntity.notFound().build());
                });
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientProfileDto> updateMyProfile(Authentication authentication, @RequestBody PatientProfileDto profileDto) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    updatePatientFromDto(patient, profileDto);
                    Patient saved = patientRepository.save(patient);
                    return ResponseEntity.ok(mapToProfileDto(saved));
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
                    List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patient.getId());
                    List<LabReport> labReports = labReportRepository.findByPatientId(patient.getId());
                    List<ImagingReport> imagingReports = imagingReportRepository.findByPatientId(patient.getId());

                    long upcomingCount = appointments.stream().filter(a -> "Upcoming".equalsIgnoreCase(a.getStatus())).count();
                    long pastCount = appointments.stream().filter(a -> "Past".equalsIgnoreCase(a.getStatus())).count();
                    long pendingCount = labReports.stream().filter(r -> !"Published".equalsIgnoreCase(r.getStatus())).count();

                    int activeMedications = 0;
                    if (!prescriptions.isEmpty()) {
                        // find latest prescription
                        prescriptions.sort(Comparator.comparing(Prescription::getDate).reversed());
                        activeMedications = prescriptions.get(0).getMedicines().size();
                    }

                    Map<String, Object> summary = new HashMap<>();
                    summary.put("totalVisits", (int) (pastCount + prescriptions.size() + labReports.size()));
                    summary.put("activeTreatments", patient.getChronicDiseases() != null ? patient.getChronicDiseases().size() : 0);
                    summary.put("upcomingAppointments", (int) upcomingCount);
                    summary.put("pendingReports", (int) pendingCount);
                    summary.put("activeMedications", activeMedications);

                    return ResponseEntity.ok(summary);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dashboard/ai-summary")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getAiSummary(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    String bp = (patient.getBpSystolic() != null ? patient.getBpSystolic() : "120") + "/" + (patient.getBpDiastolic() != null ? patient.getBpDiastolic() : "80");
                    String sugar = patient.getBloodGlucose() != null ? patient.getBloodGlucose() : "110";

                    String summaryText = String.format("Based on your records, your blood pressure is currently stabilized around %s mmHg, but your fasting blood glucose is recorded at %s mg/dL. Please follow your diet advice and consult with Dr. Ahmed Chowdhury if symptoms persist.", bp, sugar);

                    Map<String, Object> response = new HashMap<>();
                    response.put("summaryText", summaryText);
                    response.put("lastUpdated", patient.getVitalsLastUpdated() != null ? patient.getVitalsLastUpdated().toString() : LocalDateTime.now().toString());

                    return ResponseEntity.ok(response);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Appointment>> getMyAppointments(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> ResponseEntity.ok(appointmentRepository.findByPatientIdOrderByDateDesc(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Appointment> bookAppointment(Authentication authentication, @RequestBody AppointmentRequestDto request) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    Doctor doctor = doctorRepository.findById(Long.parseLong(request.getDoctorId())).orElseThrow();
                    String dob = request.getDate();
                    if (dob.contains("T")) {
                        dob = dob.split("T")[0];
                    }
                    LocalDate date = LocalDate.parse(dob);

                    // Generate queue number
                    List<Appointment> existing = appointmentRepository.findByPatientId(patient.getId());
                    int count = 1;
                    for (Appointment app : existing) {
                        if (app.getDoctor().getId().equals(doctor.getId()) && app.getDate().equals(date)) {
                            count++;
                        }
                    }
                    String queueNo = String.format("Q-%02d", count);
                    String appId = "APP-" + System.currentTimeMillis() % 1000000;

                    Appointment appointment = Appointment.builder()
                            .id(appId)
                            .patient(patient)
                            .doctor(doctor)
                            .date(date)
                            .timeSlot(request.getTimeSlot())
                            .queueNumber(queueNo)
                            .status("Upcoming")
                            .hospitalName(doctor.getHospitalAffiliation() != null ? doctor.getHospitalAffiliation() : "Dhaka Central Hospital")
                            .build();

                    return ResponseEntity.ok(appointmentRepository.save(appointment));
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/appointments/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> cancelAppointment(Authentication authentication, @PathVariable String id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus("Cancelled");
            appointmentRepository.save(appointment);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Prescription>> getMyPrescriptions(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> ResponseEntity.ok(prescriptionRepository.findByPatientIdOrderByDateDesc(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/lab-reports")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<LabReport>> getMyLabReports(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> ResponseEntity.ok(labReportRepository.findByPatientIdOrderByDateDesc(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/imaging-reports")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<ImagingReport>> getMyImagingReports(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> ResponseEntity.ok(imagingReportRepository.findByPatientIdOrderByDateDesc(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me/timeline")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<Map<String, Object>>> getMyTimeline(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .map(patient -> {
                    List<Map<String, Object>> events = new ArrayList<>();

                    // Load records
                    List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
                    List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patient.getId());
                    List<LabReport> labReports = labReportRepository.findByPatientId(patient.getId());
                    List<ImagingReport> imagingReports = imagingReportRepository.findByPatientId(patient.getId());

                    // format events
                    for (Appointment app : appointments) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", app.getId());
                        event.put("type", "consultation");
                        event.put("title", "Consultation with " + app.getDoctor().getFullName());
                        event.put("description", "Specialization: " + app.getDoctor().getSpecialization());
                        event.put("date", app.getDate().toString() + "T09:00:00.000");
                        event.put("doctorName", app.getDoctor().getFullName());
                        event.put("hospitalName", app.getHospitalName());
                        event.put("referenceId", app.getId());
                        events.add(event);
                    }

                    for (Prescription p : prescriptions) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", p.getId());
                        event.put("type", "prescription");
                        event.put("title", "Prescription for " + p.getDiagnosis());
                        event.put("description", p.getClinicalNotes());
                        event.put("date", p.getDate().toString());
                        event.put("doctorName", p.getDoctorName());
                        event.put("hospitalName", p.getHospitalName());
                        event.put("referenceId", p.getId());
                        events.add(event);
                    }

                    for (LabReport r : labReports) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", r.getId());
                        event.put("type", "labTest");
                        event.put("title", r.getTestName());
                        event.put("description", "Category: " + r.getCategory() + " (Results: " + r.getStatus() + ")");
                        event.put("date", r.getDate().toString());
                        event.put("doctorName", r.getDoctorName());
                        event.put("hospitalName", r.getHospitalName());
                        event.put("referenceId", r.getId());
                        events.add(event);
                    }

                    for (ImagingReport i : imagingReports) {
                        Map<String, Object> event = new HashMap<>();
                        event.put("id", i.getId());
                        event.put("type", "imaging");
                        event.put("title", i.getType() + " of " + i.getBodyPart());
                        event.put("description", i.getImpression());
                        event.put("date", i.getDate().toString());
                        event.put("doctorName", i.getDoctorName());
                        event.put("hospitalName", i.getHospitalName());
                        event.put("referenceId", i.getId());
                        events.add(event);
                    }

                    // sort descending by date
                    events.sort((a, b) -> {
                        String da = (String) a.get("date");
                        String db = (String) b.get("date");
                        return db.compareTo(da);
                    });

                    return ResponseEntity.ok(events);
                }).orElse(ResponseEntity.notFound().build());
    }

    private PatientProfileDto mapToProfileDto(Patient p) {
        PatientProfileDto dto = new PatientProfileDto();
        dto.setHealthId("NUD-000-" + p.getId());
        dto.setName(p.getFullName());
        dto.setDateOfBirth(p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : null);
        dto.setGender(p.getGender());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setNationalId(p.getNationalId());
        dto.setPhone(p.getContactNumber());
        dto.setOccupation(p.getOccupation());
        dto.setMaritalStatus(p.getMaritalStatus());
        dto.setPresentAddress(p.getPresentAddress());
        dto.setPermanentAddress(p.getPermanentAddress());

        List<EmergencyContactDto> contacts = new ArrayList<>();
        if (p.getEmergencyContactName() != null || p.getEmergencyContactRelation() != null || p.getEmergencyContactPhone() != null) {
            EmergencyContactDto ice = new EmergencyContactDto();
            ice.setName(p.getEmergencyContactName());
            ice.setRelationship(p.getEmergencyContactRelation());
            ice.setPhone(p.getEmergencyContactPhone());
            contacts.add(ice);
        }
        dto.setEmergencyContacts(contacts);

        List<AllergyDto> allergies = new ArrayList<>();
        if (p.getAllergies() != null) {
            for (PatientAllergy allergy : p.getAllergies()) {
                AllergyDto aDto = new AllergyDto();
                aDto.setAllergen(allergy.getAllergen());
                aDto.setSeverity(allergy.getSeverity());
                aDto.setReaction(allergy.getReaction());
                allergies.add(aDto);
            }
        }
        dto.setAllergies(allergies);

        List<ChronicDiseaseDto> chronic = new ArrayList<>();
        if (p.getChronicDiseases() != null) {
            for (PatientChronicDisease disease : p.getChronicDiseases()) {
                ChronicDiseaseDto dDto = new ChronicDiseaseDto();
                dDto.setDiseaseName(disease.getDiseaseName());
                dDto.setStatus(disease.getStatus());
                dDto.setDiagnosedDate(disease.getDiagnosedDate() != null ? disease.getDiagnosedDate().toString() : null);
                chronic.add(dDto);
            }
        }
        dto.setChronicDiseases(chronic);

        VitalSignDto vitals = new VitalSignDto();
        vitals.setBpSystolic(p.getBpSystolic() != null ? p.getBpSystolic() : "");
        vitals.setBpDiastolic(p.getBpDiastolic() != null ? p.getBpDiastolic() : "");
        vitals.setBloodGlucose(p.getBloodGlucose() != null ? p.getBloodGlucose() : "");
        vitals.setHeartRate(p.getHeartRate() != null ? p.getHeartRate() : "");
        vitals.setWeight(p.getWeight() != null ? p.getWeight() : "");
        vitals.setLastUpdated(p.getVitalsLastUpdated() != null ? p.getVitalsLastUpdated().toString() : null);
        dto.setVitals(vitals);

        return dto;
    }

    private void updatePatientFromDto(Patient p, PatientProfileDto dto) {
        if (dto.getName() != null) p.setFullName(dto.getName());
        if (dto.getDateOfBirth() != null) {
            String dob = dto.getDateOfBirth();
            if (dob.contains("T")) {
                dob = dob.split("T")[0];
            }
            p.setDateOfBirth(LocalDate.parse(dob));
        }
        if (dto.getGender() != null) p.setGender(dto.getGender());
        if (dto.getBloodGroup() != null) p.setBloodGroup(dto.getBloodGroup());
        if (dto.getNationalId() != null) p.setNationalId(dto.getNationalId());
        if (dto.getPhone() != null) p.setContactNumber(dto.getPhone());
        if (dto.getOccupation() != null) p.setOccupation(dto.getOccupation());
        if (dto.getMaritalStatus() != null) p.setMaritalStatus(dto.getMaritalStatus());
        if (dto.getPresentAddress() != null) p.setPresentAddress(dto.getPresentAddress());
        if (dto.getPermanentAddress() != null) p.setPermanentAddress(dto.getPermanentAddress());

        if (dto.getEmergencyContacts() != null && !dto.getEmergencyContacts().isEmpty()) {
            EmergencyContactDto ice = dto.getEmergencyContacts().get(0);
            p.setEmergencyContactName(ice.getName());
            p.setEmergencyContactRelation(ice.getRelationship());
            p.setEmergencyContactPhone(ice.getPhone());
        }

        if (dto.getAllergies() != null) {
            p.getAllergies().clear();
            for (AllergyDto aDto : dto.getAllergies()) {
                p.getAllergies().add(PatientAllergy.builder()
                        .patient(p)
                        .allergen(aDto.getAllergen())
                        .reaction(aDto.getReaction())
                        .severity(aDto.getSeverity())
                        .build());
            }
        }

        if (dto.getChronicDiseases() != null) {
            p.getChronicDiseases().clear();
            for (ChronicDiseaseDto cdDto : dto.getChronicDiseases()) {
                String diagnosedStr = cdDto.getDiagnosedDate();
                if (diagnosedStr != null && diagnosedStr.contains("T")) {
                    diagnosedStr = diagnosedStr.split("T")[0];
                }
                LocalDate diagnosedDate = diagnosedStr != null ? LocalDate.parse(diagnosedStr) : null;
                p.getChronicDiseases().add(PatientChronicDisease.builder()
                        .patient(p)
                        .diseaseName(cdDto.getDiseaseName())
                        .status(cdDto.getStatus())
                        .diagnosedDate(diagnosedDate)
                        .build());
            }
        }

        if (dto.getVitals() != null) {
            VitalSignDto vDto = dto.getVitals();
            p.setBpSystolic(vDto.getBpSystolic());
            p.setBpDiastolic(vDto.getBpDiastolic());
            p.setBloodGlucose(vDto.getBloodGlucose());
            p.setHeartRate(vDto.getHeartRate());
            p.setWeight(vDto.getWeight());
            p.setVitalsLastUpdated(LocalDateTime.now());
        }
    }

    @Data
    public static class PatientProfileDto {
        private String healthId;
        private String name;
        private String dateOfBirth;
        private String gender;
        private String bloodGroup;
        private String nationalId;
        private String phone;
        private String occupation;
        private String maritalStatus;
        private String presentAddress;
        private String permanentAddress;
        private List<EmergencyContactDto> emergencyContacts;
        private List<AllergyDto> allergies;
        private List<ChronicDiseaseDto> chronicDiseases;
        private VitalSignDto vitals;
    }

    @Data
    public static class EmergencyContactDto {
        private String name;
        private String relationship;
        private String phone;
    }

    @Data
    public static class AllergyDto {
        private String allergen;
        private String severity;
        private String reaction;
    }

    @Data
    public static class ChronicDiseaseDto {
        private String diseaseName;
        private String status;
        private String diagnosedDate;
    }

    @Data
    public static class VitalSignDto {
        private String bpSystolic;
        private String bpDiastolic;
        private String bloodGlucose;
        private String heartRate;
        private String weight;
        private String lastUpdated;
    }

    @Data
    public static class AppointmentRequestDto {
        private String doctorId;
        private String date;
        private String timeSlot;
    }
}

