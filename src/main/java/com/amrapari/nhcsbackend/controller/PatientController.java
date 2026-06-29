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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientRepository patientRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

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

    @PostMapping("/ai-suggest")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> aiSuggest(Authentication authentication, @RequestBody Map<String, String> request) {
        String problemText = request.get("problemText");
        if (problemText == null) problemText = "";
        
        String summaryEn = "Patient description analyzed: ";
        String summaryBn = "রোগীর বিবরণ বিশ্লেষণ করা হয়েছে: ";
        String specialization = "General Medicine";
        
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            apiKey = geminiApiKey;
        }
        boolean geminiSuccess = false;
        
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("YOUR_")) {
            Map<String, String> geminiRes = callGemini(problemText, apiKey);
            if (geminiRes != null && geminiRes.containsKey("specialization")) {
                specialization = geminiRes.get("specialization");
                summaryEn = geminiRes.get("summaryEn");
                summaryBn = geminiRes.get("summaryBn");
                geminiSuccess = true;
            }
        }
        
        if (!geminiSuccess) {
            String cleanText = problemText.toLowerCase();
            
            if (cleanText.contains("বুক") || cleanText.contains("chest") || cleanText.contains("হার্ট") || cleanText.contains("heart") || cleanText.contains("ব্যথা") && (cleanText.contains("বুকের") || cleanText.contains("নিঃশ্বাস"))) {
                specialization = "Cardiology";
                summaryEn = "Patient reports chest pain and difficulty breathing. Recommended consultation with a Cardiologist.";
                summaryBn = "রোগী বুকে ব্যথা এবং শ্বাসকষ্টের কথা জানিয়েছেন। হৃদরোগ বিশেষজ্ঞের (Cardiologist) পরামর্শ নেওয়ার সুপারিশ করা হচ্ছে।";
            } else if (cleanText.contains("ডায়াবেটিস") || cleanText.contains("diabetes") || cleanText.contains("চিনি") || cleanText.contains("sugar") || cleanText.contains("হরমোন") || cleanText.contains("hormone")) {
                specialization = "Endocrinology & Diabetology";
                summaryEn = "Patient reports diabetes-related issues or blood sugar fluctuations. Recommended consultation with an Endocrinologist.";
                summaryBn = "রোগী ডায়াবেটিস বা রক্তে শর্করার তারতম্যের সমস্যা জানিয়েছেন। হরমোন ও ডায়াবেটিস বিশেষজ্ঞের (Endocrinologist) পরামর্শ নেওয়ার সুপারিশ করা হচ্ছে।";
            } else if (cleanText.contains("গর্ভবতী") || cleanText.contains("pregnant") || cleanText.contains("মহিলা") || cleanText.contains("নারী") || cleanText.contains("gynae") || cleanText.contains("গাইনি") || cleanText.contains("তলপেট")) {
                specialization = "Gynaecology & Obstetrics";
                summaryEn = "Patient reports pregnancy or gynaecological symptoms. Recommended consultation with a Gynaecologist.";
                summaryBn = "রোগী গর্ভাবস্থা বা স্ত্রী-রোগ সংক্রান্ত সমস্যা জানিয়েছেন। গাইনি ও প্রসূতি বিশেষজ্ঞের (Gynaecologist) পরামর্শ নেওয়ার সুপারিশ করা হচ্ছে।";
            } else {
                specialization = "General Medicine";
                summaryEn = "Patient reports general symptoms (fever, weakness, or body ache). Recommended consultation with a General Physician.";
                summaryBn = "রোগী সাধারণ শারীরিক সমস্যা (জ্বর, দুর্বলতা বা শরীর ব্যথা) জানিয়েছেন। জেনারেল মেডিসিন বিশেষজ্ঞের পরামর্শ নেওয়ার সুপারিশ করা হচ্ছে।";
            }
        }
        
        // Find doctors matching specialization
        String finalSpecialization = specialization;
        List<Doctor> matchedDoctors = doctorRepository.findAll().stream()
                .filter(d -> {
                    String docSpec = d.getSpecialization().toLowerCase();
                    String spec = finalSpecialization.toLowerCase();
                    if (spec.equals("general medicine")) {
                        return docSpec.equals("general medicine");
                    }
                    if (spec.equals("gynaecology & obstetrics")) {
                        return docSpec.contains("gynaecology") || docSpec.contains("obstetrics");
                    }
                    if (spec.equals("endocrinology & diabetology")) {
                        return docSpec.contains("endocrinology") || docSpec.contains("diabetology");
                    }
                    if (spec.equals("cardiology")) {
                        return docSpec.contains("cardiology");
                    }
                    return docSpec.contains(spec.split(" ")[0]);
                })
                .collect(Collectors.toList());
                
        // If none found, fallback to all doctors
        if (matchedDoctors.isEmpty()) {
            matchedDoctors = doctorRepository.findAll();
        }
        
        // Find suggested hospitals dynamically from the database
        List<Hospital> allHospitals = hospitalRepository.findAll();
        List<Map<String, Object>> suggestedHospitals = new ArrayList<>();
        
        // Sort/filter hospitals based on relevance to specialization
        List<Hospital> sortedHospitals = allHospitals.stream()
                .sorted((h1, h2) -> {
                    String spec = finalSpecialization.toLowerCase();
                    boolean h1Match = false;
                    boolean h2Match = false;
                    
                    if (spec.contains("cardiology") || spec.contains("cardiovascular")) {
                        h1Match = h1.getName().toLowerCase().contains("cardiovascular") || h1.getName().toLowerCase().contains("heart");
                        h2Match = h2.getName().toLowerCase().contains("cardiovascular") || h2.getName().toLowerCase().contains("heart");
                    } else if (spec.contains("urology") || spec.contains("kidney")) {
                        h1Match = h1.getName().toLowerCase().contains("kidney") || h1.getName().toLowerCase().contains("urology");
                        h2Match = h2.getName().toLowerCase().contains("kidney") || h2.getName().toLowerCase().contains("urology");
                    } else if (spec.contains("pediatric") || spec.contains("shishu")) {
                        h1Match = h1.getName().toLowerCase().contains("shishu") || h1.getName().toLowerCase().contains("child");
                        h2Match = h2.getName().toLowerCase().contains("shishu") || h2.getName().toLowerCase().contains("child");
                    } else if (spec.contains("cancer") || spec.contains("oncology")) {
                        h1Match = h1.getName().toLowerCase().contains("cancer") || h1.getName().toLowerCase().contains("tumor");
                        h2Match = h2.getName().toLowerCase().contains("cancer") || h2.getName().toLowerCase().contains("tumor");
                    }
                    
                    if (h1Match && !h2Match) return -1;
                    if (!h1Match && h2Match) return 1;
                    
                    // Fallback: sort by compliance score descending
                    return Integer.compare(h2.getComplianceScore(), h1.getComplianceScore());
                })
                .limit(3)
                .collect(Collectors.toList());
                
        // Map to response format
        for (Hospital h : sortedHospitals) {
            Map<String, Object> hMap = new HashMap<>();
            hMap.put("name", h.getName());
            
            // Generate a realistic address based on division
            String address = h.getDivision() + ", Bangladesh";
            if (h.getDivision().equalsIgnoreCase("Dhaka")) {
                if (h.getName().contains("United")) {
                    address = "Gulshan, Dhaka";
                } else if (h.getName().contains("Square")) {
                    address = "Panthapath, Dhaka";
                } else if (h.getName().contains("Labaid") || h.getName().contains("Ibn Sina")) {
                    address = "Dhanmondi, Dhaka";
                } else {
                    address = "Dhaka, Bangladesh";
                }
            }
            hMap.put("address", address);
            
            // Calculate a consistent mock distance based on ID
            double dist = 0.5 + (h.getId() % 7) * 0.6;
            hMap.put("distance", String.format(Locale.US, "%.1f km", dist));
            
            suggestedHospitals.add(hMap);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("summaryEn", summaryEn);
        response.put("summaryBn", summaryBn);
        response.put("specialization", specialization);
        response.put("suggestedHospitals", suggestedHospitals);
        response.put("suggestedDoctors", matchedDoctors);
        
        return ResponseEntity.ok(response);
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

    private Map<String, String> callGemini(String problemText, String apiKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            
            String prompt = "You are a clinical symptom classification assistant. You must analyze the patient's symptom description in Bangla or English, categorize it into a medical specialization (Cardiology, Endocrinology & Diabetology, Gynaecology & Obstetrics, General Medicine, Ophthalmology, Psychiatry, Dermatology, Gastroenterology, ENT, Neuromedicine, Pulmonology, Orthopedics, Urology, Pediatrics, or General Surgery), and generate a concise health summary in both English and Bangla. Return a JSON object matching this structure: {\"specialization\": \"Specialization Name\", \"summaryEn\": \"English summary...\", \"summaryBn\": \"Bangla summary...\"}. Patient's symptom: " + problemText;

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
            } else {
                System.err.println("Gemini API returned error code: " + response.statusCode() + " Body: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Gemini API call failed: " + e.getMessage());
        }
        return null;
    }
}

