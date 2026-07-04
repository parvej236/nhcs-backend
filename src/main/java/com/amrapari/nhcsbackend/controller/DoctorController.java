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

    @GetMapping("/appointments/active")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Appointment>> getActiveAppointments(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .flatMap(user -> doctorRepository.findByUserId(user.getId()))
                .map(doctor -> {
                    List<Appointment> list = appointmentRepository.findAll().stream()
                            .filter(a -> a.getDoctor().getId().equals(doctor.getId())
                                    && "APPROVED".equals(a.getApprovalStatus())
                                    && "CHECKED_IN".equals(a.getArrivalStatus()))
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(list);
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
