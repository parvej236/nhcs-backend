package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DoctorController(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            LabReportRepository labReportRepository,
            PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.labReportRepository = labReportRepository;
        this.patientRepository = patientRepository;
    }

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
        LocalDate requestedDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Real booked slots for this doctor on the requested date (ignore rejected/cancelled).
        Set<String> bookedTimes = appointmentRepository.findByDoctorIdAndDate(id, requestedDate).stream()
                .filter(a -> !"REJECTED".equalsIgnoreCase(a.getApprovalStatus())
                        && !"Cancelled".equalsIgnoreCase(a.getStatus()))
                .map(Appointment::getTimeSlot)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Standard working-hour slots (30-min intervals): 09:00–12:00 and 16:00–17:00.
        List<LocalTime> slotTimes = new ArrayList<>();
        for (LocalTime t = LocalTime.of(9, 0); !t.isAfter(LocalTime.of(12, 0)); t = t.plusMinutes(30)) {
            slotTimes.add(t);
        }
        for (LocalTime t = LocalTime.of(16, 0); !t.isAfter(LocalTime.of(17, 0)); t = t.plusMinutes(30)) {
            slotTimes.add(t);
        }

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
        List<Map<String, Object>> slots = new ArrayList<>();
        int index = 1;
        for (LocalTime t : slotTimes) {
            String label = labelFmt.format(t);

            boolean available = true;
            if (requestedDate.isBefore(today)) {
                available = false;                       // past dates: nothing available
            } else if (requestedDate.isEqual(today) && !t.isAfter(now)) {
                available = false;                       // today: slot time already passed
            } else if (bookedTimes.contains(label)) {
                available = false;                       // already booked by someone
            }

            slots.add(Map.of("id", "TS-" + index, "time", label, "isAvailable", available));
            index++;
        }
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/appointments/active")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getActiveAppointments(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(doctor -> {
                    List<Appointment> list = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor().getId().equals(doctor.getId())
                                    && "APPROVED".equals(a.getApprovalStatus())
                                    && "CHECKED_IN".equals(a.getArrivalStatus())
                                    && !"Cancelled".equalsIgnoreCase(a.getStatus()))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(list);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getSchedule(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(doctor -> {
                    List<Appointment> list = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor() != null
                                    && a.getDoctor().getId().equals(doctor.getId())
                                    && a.getDate() != null
                                    && !"REJECTED".equals(a.getApprovalStatus())
                                    && !"Cancelled".equalsIgnoreCase(a.getStatus()))
                            .sorted(Comparator
                                    .comparing(Appointment::getDate)
                                    .thenComparing(a -> a.getTimeSlot() != null ? a.getTimeSlot() : ""))
                            .collect(Collectors.toList());

                    List<Map<String, Object>> response = list.stream().map(a -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", a.getId());
                        map.put("date", a.getDate().toString());
                        map.put("dayOfWeek", a.getDate().getDayOfWeek().toString()); // MONDAY, TUESDAY, ...
                        map.put("timeSlot", a.getTimeSlot() != null ? a.getTimeSlot() : "");
                        map.put("patientName", a.getPatientName() != null ? a.getPatientName() : "Unknown");
                        map.put("visitType", a.getVisitType() != null ? a.getVisitType() : "Consultation");
                        map.put("department", doctor.getSpecialization() != null ? doctor.getSpecialization() : "General");
                        map.put("approvalStatus", a.getApprovalStatus());
                        map.put("arrivalStatus", a.getArrivalStatus());
                        return map;
                    }).collect(Collectors.toList());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Doctor> getDoctorProfile(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Doctor> updateDoctorProfile(
            Authentication authentication,
            @RequestBody Doctor updatedDoctor) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(doctor -> {
                    doctor.setFullName(updatedDoctor.getFullName());
                    doctor.setSpecialization(updatedDoctor.getSpecialization());
                    doctor.setLicenseNumber(updatedDoctor.getLicenseNumber());
                    doctor.setContactNumber(updatedDoctor.getContactNumber());
                    doctor.setHospitalAffiliation(updatedDoctor.getHospitalAffiliation());
                    doctor.setExperienceYears(updatedDoctor.getExperienceYears());
                    doctor.setConsultationFee(updatedDoctor.getConsultationFee());
                    Doctor saved = doctorRepository.save(doctor);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reports/pending")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Map<String, Object>>> getPendingReports(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(doctor -> {
                    List<LabReport> list = labReportRepository.findAll().stream()
                            .filter(r -> r.getDoctorName() != null && r.getDoctorName().contains(doctor.getFullName()))
                            .collect(Collectors.toList());
                    
                    List<Map<String, Object>> response = list.stream().map(r -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", r.getId());
                        map.put("patientName", r.getPatient() != null ? r.getPatient().getFullName() : "Unknown");
                        map.put("healthId", r.getPatient() != null ? r.getPatient().getNationalId() : "");
                        map.put("testName", r.getTestName());
                        map.put("category", r.getCategory());
                        map.put("orderedDate", r.getDate() != null ? r.getDate().toString() : LocalDateTime.now().toString());
                        map.put("status", r.getStatus());
                        map.put("trendSummary", r.getAiInterpretation() != null ? r.getAiInterpretation() : "No trend summary");
                        map.put("trendStatus", "Stable");
                        
                        Map<String, String> results = new HashMap<>();
                        if (r.getResults() != null) {
                            for (LabTestResult res : r.getResults()) {
                                results.put(res.getParameter(), res.getValue() + " " + res.getUnit() + " (" + res.getStatus() + ")");
                            }
                        }
                        map.put("results", results);
                        return map;
                    }).collect(Collectors.toList());
                    
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/appointments/{appointmentId}/prescribe")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> prescribe(
            @PathVariable String appointmentId,
            @RequestBody PrescriptionRequestDto dto) {
        Appointment app = appointmentRepository.findById(appointmentId).orElseThrow();
        Patient patient = app.getPatient();

        String prescriptionId = "PR-" + System.currentTimeMillis() % 1000000;
        
        Prescription prescription = Prescription.builder()
                .id(prescriptionId)
                .patient(patient)
                .date(LocalDateTime.now())
                .doctorName(app.getDoctor().getFullName())
                .doctorSpecialization(app.getDoctor().getSpecialization())
                .hospitalName(app.getHospitalName())
                .diagnosis(dto.getDiagnosis())
                .clinicalNotes(dto.getClinicalNotes())
                .followUpDate(dto.getFollowUpDate())
                .medicines(new ArrayList<>())
                .build();

        if (dto.getMedicines() != null) {
            for (MedicineDto medDto : dto.getMedicines()) {
                Medicine med = Medicine.builder()
                        .prescription(prescription)
                        .name(medDto.getName())
                        .dosage(medDto.getDosage())
                        .instruction(medDto.getInstruction())
                        .duration(medDto.getDuration())
                        .build();
                prescription.getMedicines().add(med);
            }
        }

        prescriptionRepository.save(prescription);

        // Update appointment status to COMPLETED
        app.setStatus("Completed");
        app.setArrivalStatus("COMPLETED");
        appointmentRepository.save(app);

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "prescriptionId", prescriptionId));
    }

    @PostMapping("/appointments/{appointmentId}/lab-order")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createLabOrder(
            @PathVariable String appointmentId,
            @RequestBody LabOrderRequestDto dto) {
        Appointment app = appointmentRepository.findById(appointmentId).orElseThrow();
        Patient patient = app.getPatient();

        String labReportId = "LR-" + System.currentTimeMillis() % 1000000;
        
        LabReport report = LabReport.builder()
                .id(labReportId)
                .patient(patient)
                .testName(dto.getTestName())
                .category(dto.getCategory())
                .date(LocalDateTime.now())
                .hospitalName(app.getHospitalName())
                .doctorName(app.getDoctor().getFullName())
                .status("PENDING")
                .results(new ArrayList<>())
                .build();

        labReportRepository.save(report);

        return ResponseEntity.ok(Map.of("status", "SUCCESS", "labReportId", labReportId));
    }

    // Sequence used to keep lab-report ids unique when several tests are ordered
    // within the same millisecond during a single treatment submission.
    private static final java.util.concurrent.atomic.AtomicLong LAB_SEQ = new java.util.concurrent.atomic.AtomicLong(0);

    /**
     * Resolves the real patient behind a Unified Health ID. Health IDs are
     * formatted "NUD-000-<patientId>", so the trailing numeric segment is the
     * patient id. Falls back to treating the whole string as a numeric id.
     */
    private Optional<Patient> resolvePatientByHealthId(String healthId) {
        if (healthId == null || healthId.isBlank()) return Optional.empty();
        String trimmed = healthId.trim();
        int lastDash = trimmed.lastIndexOf('-');
        if (lastDash >= 0 && lastDash < trimmed.length() - 1) {
            try {
                return patientRepository.findById(Long.parseLong(trimmed.substring(lastDash + 1)));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        try {
            return patientRepository.findById(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private Doctor resolveAuthenticatedDoctor(Authentication authentication) {
        if (authentication == null) return null;
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .orElse(null);
    }

    /**
     * Writes a prescription straight to a patient's permanent record, keyed by
     * their Unified Health ID. This is what the doctor's "Submit Treatment"
     * button calls — it works whether the consultation started from the live
     * queue or from a health-ID search, so the prescription reliably lands in
     * the real patient's Medical Vault (/patients/me/prescriptions).
     */
    @PostMapping("/patients/{healthId}/prescribe")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> prescribeByHealthId(
            Authentication authentication,
            @PathVariable String healthId,
            @RequestBody PrescriptionRequestDto dto) {
        Patient patient = resolvePatientByHealthId(healthId).orElse(null);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        Doctor doctor = resolveAuthenticatedDoctor(authentication);

        String prescriptionId = "PR-" + System.currentTimeMillis() % 1000000;

        Prescription prescription = Prescription.builder()
                .id(prescriptionId)
                .patient(patient)
                .date(LocalDateTime.now())
                .doctorName(doctor != null ? doctor.getFullName() : "Attending Physician")
                .doctorSpecialization(doctor != null ? doctor.getSpecialization() : "General Medicine")
                .hospitalName(doctor != null && doctor.getHospitalAffiliation() != null
                        ? doctor.getHospitalAffiliation() : "Dhaka Central Hospital")
                .diagnosis(dto.getDiagnosis())
                .clinicalNotes(dto.getClinicalNotes())
                .followUpDate(dto.getFollowUpDate())
                .medicines(new ArrayList<>())
                .build();

        if (dto.getMedicines() != null) {
            for (MedicineDto medDto : dto.getMedicines()) {
                prescription.getMedicines().add(Medicine.builder()
                        .prescription(prescription)
                        .name(medDto.getName())
                        .dosage(medDto.getDosage())
                        .instruction(medDto.getInstruction())
                        .duration(medDto.getDuration())
                        .build());
            }
        }

        prescriptionRepository.save(prescription);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "prescriptionId", prescriptionId));
    }

    /**
     * Raises a lab-test request for a patient, keyed by their Unified Health ID.
     * The order is persisted with status "PENDING" so it surfaces in the
     * hospital Laboratory queue (/hospitals/lab-orders); once the lab publishes
     * results it flows into the patient's Medical Vault.
     */
    @PostMapping("/patients/{healthId}/lab-order")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> createLabOrderByHealthId(
            Authentication authentication,
            @PathVariable String healthId,
            @RequestBody LabOrderRequestDto dto) {
        Patient patient = resolvePatientByHealthId(healthId).orElse(null);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        Doctor doctor = resolveAuthenticatedDoctor(authentication);

        String labReportId = "LR-" + (System.currentTimeMillis() % 1000000) + "-" + LAB_SEQ.incrementAndGet();

        LabReport report = LabReport.builder()
                .id(labReportId)
                .patient(patient)
                .testName(dto.getTestName())
                .category(dto.getCategory())
                .date(LocalDateTime.now())
                .hospitalName(doctor != null && doctor.getHospitalAffiliation() != null
                        ? doctor.getHospitalAffiliation() : "Dhaka Central Hospital")
                .doctorName(doctor != null ? doctor.getFullName() : "Attending Physician")
                .status("PENDING")
                .results(new ArrayList<>())
                .build();

        labReportRepository.save(report);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "labReportId", labReportId));
    }

    @PostMapping("/appointments/{appointmentId}/ai-briefing")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Object>> getAiBriefing(
            @PathVariable String appointmentId,
            @Value("${gemini.api.key:}") String geminiApiKey) {
        
        Appointment app = appointmentRepository.findById(appointmentId).orElseThrow();
        Patient patient = app.getPatient();

        StringBuilder historyBuilder = new StringBuilder();
        historyBuilder.append("Patient Name: ").append(patient.getFullName()).append("\n");
        historyBuilder.append("Age: ").append(patient.getDateOfBirth() != null ? (LocalDate.now().getYear() - patient.getDateOfBirth().getYear()) : "40").append("\n");
        historyBuilder.append("Gender: ").append(patient.getGender()).append("\n");
        historyBuilder.append("Blood Group: ").append(patient.getBloodGroup()).append("\n");
        
        historyBuilder.append("Current Vitals: \n");
        historyBuilder.append("- Blood Pressure: ").append(patient.getBpSystolic()).append("/").append(patient.getBpDiastolic()).append(" mmHg\n");
        historyBuilder.append("- Blood Glucose: ").append(patient.getBloodGlucose()).append(" mg/dL\n");
        historyBuilder.append("- Heart Rate: ").append(patient.getHeartRate()).append(" bpm\n");
        historyBuilder.append("- Weight: ").append(patient.getWeight()).append(" kg\n\n");

        historyBuilder.append("Allergies: \n");
        if (patient.getAllergies() == null || patient.getAllergies().isEmpty()) {
            historyBuilder.append("- None recorded\n");
        } else {
            for (PatientAllergy allergy : patient.getAllergies()) {
                historyBuilder.append("- ").append(allergy.getAllergen()).append(" (Severity: ").append(allergy.getSeverity()).append(")\n");
            }
        }
        historyBuilder.append("\n");

        historyBuilder.append("Chronic Diseases: \n");
        if (patient.getChronicDiseases() == null || patient.getChronicDiseases().isEmpty()) {
            historyBuilder.append("- None recorded\n");
        } else {
            for (PatientChronicDisease disease : patient.getChronicDiseases()) {
                historyBuilder.append("- ").append(disease.getDiseaseName()).append(" (Diagnosed: ").append(disease.getDiagnosedDate()).append(")\n");
            }
        }
        historyBuilder.append("\n");

        List<Prescription> pastPrescriptions = prescriptionRepository.findByPatientIdOrderByDateDesc(patient.getId());
        historyBuilder.append("Past Prescriptions history: \n");
        if (pastPrescriptions.isEmpty()) {
            historyBuilder.append("- None recorded\n");
        } else {
            for (Prescription pr : pastPrescriptions) {
                historyBuilder.append("- Date: ").append(pr.getDate().toString().split("T")[0])
                              .append(", Diagnosis: ").append(pr.getDiagnosis())
                              .append(", Clinical Notes: ").append(pr.getClinicalNotes()).append("\n");
            }
        }

        String clinicalHistoryText = historyBuilder.toString();
        String briefing = "";

        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            apiKey = geminiApiKey;
        }

        boolean geminiSuccess = false;
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("YOUR_")) {
            try {
                Map<String, String> geminiRes = callBriefingGemini(clinicalHistoryText, apiKey);
                if (geminiRes != null && geminiRes.containsKey("briefing")) {
                    briefing = geminiRes.get("briefing");
                    geminiSuccess = true;
                }
            } catch (Exception e) {
                System.err.println("Gemini briefing call failed: " + e.getMessage());
            }
        }

        if (!geminiSuccess) {
            StringBuilder fallback = new StringBuilder();
            fallback.append("### Patient Medical Summary (Deterministic Fallback)\n\n");
            fallback.append("**Patient:** ").append(patient.getFullName()).append("\n");
            fallback.append("**BP:** ").append(patient.getBpSystolic() != null ? patient.getBpSystolic() : "120").append("/").append(patient.getBpDiastolic() != null ? patient.getBpDiastolic() : "80").append(" mmHg\n");
            fallback.append("**Glucose:** ").append(patient.getBloodGlucose() != null ? patient.getBloodGlucose() : "95").append(" mg/dL\n");
            fallback.append("\n**Allergies:** ");
            if (patient.getAllergies() != null && !patient.getAllergies().isEmpty()) {
                patient.getAllergies().forEach(a -> fallback.append(a.getAllergen()).append(", "));
            } else {
                fallback.append("None");
            }
            fallback.append("\n**Chronic Conditions:** ");
            if (patient.getChronicDiseases() != null && !patient.getChronicDiseases().isEmpty()) {
                patient.getChronicDiseases().forEach(d -> fallback.append(d.getDiseaseName()).append(", "));
            } else {
                fallback.append("None");
            }
            fallback.append("\n\n*Review vitals and allergy records before clinical diagnosis.*");
            briefing = fallback.toString();
        }

        return ResponseEntity.ok(Map.of("briefing", briefing));
    }

    private Map<String, String> callBriefingGemini(String clinicalHistoryText, String apiKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            
            String prompt = "You are a clinical history summarizer. Given the following patient's clinical history (allergies, chronic diseases, past prescriptions), generate a structured, concise medical history briefing for a consulting doctor. Highlight key chronic risks and active medications. Return a JSON object with a single key 'briefing' containing the formatted markdown briefing. Clinical history: " + clinicalHistoryText;

            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("parts", List.of(part));
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("responseMimeType", "application/json");
            
            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(contentItem));
            body.put("generationConfig", generationConfig);
            
            String requestBody = objectMapper.writeValueAsString(body);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Map<String, Object> jsonResponse = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) jsonResponse.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            String text = (String) parts.get(0).get("text");
                            if (text != null) {
                                text = text.trim();
                                if (text.startsWith("```")) {
                                    int firstNewline = text.indexOf('\n');
                                    int lastBackticks = text.lastIndexOf("```");
                                    if (firstNewline != -1 && lastBackticks > firstNewline) {
                                        text = text.substring(firstNewline, lastBackticks).trim();
                                    }
                                }
                                return objectMapper.readValue(text, Map.class);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini history briefing call failed: " + e.getMessage());
        }
        return null;
    }

    public static class PrescriptionRequestDto {
        private String diagnosis;
        private String clinicalNotes;
        private String followUpDate;
        private List<MedicineDto> medicines;

        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

        public String getClinicalNotes() { return clinicalNotes; }
        public void setClinicalNotes(String clinicalNotes) { this.clinicalNotes = clinicalNotes; }

        public String getFollowUpDate() { return followUpDate; }
        public void setFollowUpDate(String followUpDate) { this.followUpDate = followUpDate; }

        public List<MedicineDto> getMedicines() { return medicines; }
        public void setMedicines(List<MedicineDto> medicines) { this.medicines = medicines; }
    }

    public static class MedicineDto {
        private String name;
        private String dosage;
        private String instruction;
        private String duration;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }

        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
    }

    public static class LabOrderRequestDto {
        private String testName;
        private String category;

        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
