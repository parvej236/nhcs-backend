package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.Appointment;
import com.amrapari.nhcsbackend.domain.Doctor;
import com.amrapari.nhcsbackend.domain.Hospital;
import com.amrapari.nhcsbackend.domain.BloodRequest;
import com.amrapari.nhcsbackend.dto.*;
import com.amrapari.nhcsbackend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicController {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentRepository appointmentRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @GetMapping("/stats")
    public ResponseEntity<PublicStatsDto> getStats() {
        return ResponseEntity.ok(PublicStatsDto.builder()
                .patients(patientRepository.count())
                .doctors(doctorRepository.count())
                .hospitals(hospitalRepository.count())
                .build());
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<PublicDoctorDto>> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Object[]> queueCounts = appointmentRepository.countUpcomingAppointmentsByDoctorForDate(LocalDate.now());

        Map<Long, Long> doctorQueueMap = queueCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        List<PublicDoctorDto> dtoList = doctors.stream()
                .map(doc -> PublicDoctorDto.builder()
                .id(doc.getId())
                .name(doc.getFullName())
                .specialization(doc.getSpecialization())
                .hospital(doc.getHospitalAffiliation() != null ? doc.getHospitalAffiliation() : "Dhaka Central Hospital")
                .fee(doc.getConsultationFee() != null ? doc.getConsultationFee() : 800)
                .experience(doc.getExperienceYears() != null ? doc.getExperienceYears() : 5)
                .rating(doc.getRating() != null ? doc.getRating() : 4.5)
                .queueCount(doctorQueueMap.getOrDefault(doc.getId(), 0L))
                .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/vitals-analyze")
    public ResponseEntity<VitalsAnalyzeResponse> analyzeVitals(@RequestBody VitalsAnalyzeRequest request) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            apiKey = geminiApiKey;
        }

        if (apiKey != null && !apiKey.isEmpty() && !apiKey.startsWith("YOUR_")) {
            VitalsAnalyzeResponse geminiRes = callGeminiForVitals(
                    request.getSymptomsText() != null ? request.getSymptomsText() : "",
                    request.getBpSystolic(),
                    request.getBpDiastolic(),
                    request.getGlucose(),
                    apiKey
            );
            if (geminiRes != null) {
                return ResponseEntity.ok(geminiRes);
            }
        }

        // Fallback Logic
        String symptoms = request.getSymptomsText() != null ? request.getSymptomsText() : "";
        Integer sys = request.getBpSystolic();
        Integer dia = request.getBpDiastolic();
        Double glucose = request.getGlucose();

        String cleanText = symptoms.toLowerCase();
        boolean isBangla = cleanText.matches(".*[\\u0980-\\u09FF].*");

        String category = isBangla ? "সাধারণ স্বাস্থ্য স্ক্রীনিং (General Health Screening)" : "General Health Screening";
        String severity = "success";
        List<String> recommendations = new ArrayList<>();

        // Determine BP status
        String bpVal = isBangla ? "সরবরাহ করা হয়নি" : "Not provided";
        boolean bpHigh = false;
        boolean bpElevated = false;
        if (sys != null && dia != null) {
            bpVal = sys + "/" + dia + " mmHg";
            if (sys > 180 || dia > 120) {
                category = isBangla ? "উচ্চ রক্তচাপজনিত জরুরি অবস্থা (Hypertensive Crisis)" : "Hypertensive Crisis (Urgent)";
                severity = "danger";
                bpHigh = true;
            } else if (sys >= 140 || dia >= 90) {
                category = isBangla ? "দ্বিতীয় স্তরের উচ্চ রক্তচাপ (Stage 2 Hypertension)" : "Stage 2 Hypertension (High Risk)";
                severity = "danger";
                bpHigh = true;
            } else if ((sys >= 130 && sys <= 139) || (dia >= 80 && dia <= 89)) {
                category = isBangla ? "প্রথম স্তরের উচ্চ রক্তচাপ (Stage 1 Hypertension)" : "Stage 1 Hypertension";
                severity = "warning";
                bpElevated = true;
            } else if (sys >= 120 && sys <= 129 && dia < 80) {
                category = isBangla ? "উদ্বেগজনক রক্তচাপ (Elevated Blood Pressure)" : "Elevated Blood Pressure";
                severity = "warning";
                bpElevated = true;
            } else {
                bpVal += isBangla ? " (স্বাভাবিক)" : " (Normal)";
            }
        }

        // Determine glucose status
        String glucoseVal = isBangla ? "সরবরাহ করা হয়নি" : "Not provided";
        boolean glucoseHigh = false;
        boolean glucoseElevated = false;
        if (glucose != null) {
            glucoseVal = glucose + " mg/dL";
            if (glucose >= 126.0) {
                category = isBangla ? "টাইপ-২ ডায়াবেটিস ঝুঁকি (Diabetic Range)" : "Type-2 Diabetic Range (High Risk)";
                severity = "danger";
                glucoseHigh = true;
                glucoseVal += isBangla ? " (উচ্চ)" : " (High)";
            } else if (glucose >= 100.0) {
                category = isBangla ? "প্রাক-ডায়াবেটিস ঝুঁকি (Prediabetic)" : "Prediabetic Range";
                if (!"danger".equals(severity)) {
                    severity = "warning";
                }
                glucoseElevated = true;
                glucoseVal += isBangla ? " (উদ্বেগজনক)" : " (Elevated)";
            } else {
                glucoseVal += isBangla ? " (স্বাভাবিক)" : " (Normal)";
            }
        }

        // Simple symptoms keyword analysis
        if (cleanText.contains("chest") || cleanText.contains("heart") || cleanText.contains("বুক") || cleanText.contains("হার্ট") || (cleanText.contains("ব্যথা") && cleanText.contains("বুকের"))) {
            category = isBangla ? "হৃদরোগ বিশেষজ্ঞের পরামর্শ আবশ্যক (Cardiology Referral)" : "Cardiology Referral Recommended";
            severity = "danger";
            if (isBangla) {
                recommendations.add("বুকের ব্যথার জন্য অবিলম্বে একজন হৃদরোগ বিশেষজ্ঞের (Cardiologist) পরামর্শ নিন।");
                recommendations.add("যেকোনো ধরনের ভারী পরিশ্রম থেকে বিরত থাকুন।");
                recommendations.add("শারীরিক অবস্থার অবনতি হলে দ্রুত নিকটস্থ হাসপাতালের জরুরি বিভাগে যোগাযোগ করুন।");
            } else {
                recommendations.add("Consult a Cardiologist immediately for chest discomfort.");
                recommendations.add("Avoid any heavy exertion.");
                recommendations.add("Seek emergency medical services if the discomfort worsens.");
            }
        } else if (cleanText.contains("diabetes") || cleanText.contains("sugar") || cleanText.contains("ডায়াবেটিস") || cleanText.contains("চিনি")) {
            category = isBangla ? "হরমোন বিশেষজ্ঞের পরামর্শ আবশ্যক (Endocrinology Referral)" : "Endocrinology Referral Recommended";
            if (!"danger".equals(severity)) {
                severity = "warning";
            }
            if (isBangla) {
                recommendations.add("রক্তের শর্করা (Blood Sugar) নিয়ন্ত্রণের জন্য একজন হরমোন বিশেষজ্ঞের (Endocrinologist) পরামর্শ নিন।");
                recommendations.add("মিষ্টি জাতীয় ও অতিরিক্ত শর্করাযুক্ত খাবার এড়িয়ে চলুন।");
            } else {
                recommendations.add("Consult an Endocrinologist for blood sugar management.");
                recommendations.add("Eliminate refined sugars and reduce simple carbohydrates.");
            }
        } else if (cleanText.contains("pregnant") || cleanText.contains("gynae") || cleanText.contains("গর্ভবতী") || cleanText.contains("গাইনি")) {
            category = isBangla ? "স্ত্রী ও প্রসূতি বিশেষজ্ঞের পরামর্শ আবশ্যক (Gynaecology Referral)" : "Gynaecology Referral Recommended";
            if (isBangla) {
                recommendations.add("গর্ভকালীন ও প্রসূতি স্ক্রীনিংয়ের জন্য অবিলম্বে ডাক্তারের পরামর্শ নিন।");
            } else {
                recommendations.add("Schedule a maternal/gynaecological screening immediately.");
            }
        }

        // Populate standard recommendations based on metrics
        if (bpHigh) {
            if (isBangla) {
                recommendations.add("উচ্চ রক্তচাপের জটিলতা এড়াতে দ্রুত চিকিৎসকের পরামর্শ নিন।");
                recommendations.add("খাবারে অতিরিক্ত লবণ পরিহার করুন এবং মানসিক চাপমুক্ত থাকুন।");
                recommendations.add("দিনে দুবার (সকালে ও সন্ধ্যায়) রক্তচাপ পরিমাপ করে লিখে রাখুন।");
            } else {
                recommendations.add("Seek prompt clinical evaluation to prevent hypertensive complications.");
                recommendations.add("Reduce sodium intake and avoid high-stress situations.");
                recommendations.add("Log blood pressure twice daily (morning/evening).");
            }
        } else if (bpElevated) {
            if (isBangla) {
                recommendations.add("নিয়মিত বাড়িতে রক্তচাপ পর্যবেক্ষণ করুন।");
                recommendations.add("খাদ্যাভ্যাস পরিবর্তন করুন (অল্প লবণাক্ত খাবার) এবং মানসিক চাপ কমান।");
            } else {
                recommendations.add("Monitor blood pressure regularly at home.");
                recommendations.add("Incorporate dietary adjustments (DASH diet) and reduce stress.");
            }
        }

        if (glucoseHigh) {
            if (isBangla) {
                recommendations.add("HbA1c পরীক্ষা করার জন্য একজন বিশেষজ্ঞের পরামর্শ নিন।");
                recommendations.add("মিষ্টি জাতীয় খাবার পরিহার করুন এবং কার্বোহাইড্রেট গ্রহণের পরিমাণ কমান।");
            } else {
                recommendations.add("Consult a specialist to schedule an HbA1c test.");
                recommendations.add("Eliminate refined sugars and reduce simple carbohydrates.");
            }
        } else if (glucoseElevated) {
            if (isBangla) {
                recommendations.add("সপ্তাহে অন্তত ১৫০ মিনিট মাঝারি ধরনের ব্যায়াম বা হাঁটাহাঁটি করুন।");
                recommendations.add("কম গ্লাইসেমিক ইনডেক্সযুক্ত সুষম খাবার গ্রহণ করুন।");
            } else {
                recommendations.add("Incorporate moderate aerobic physical activity (150 mins/week).");
                recommendations.add("Adopt a balanced low-glycemic index diet.");
            }
        }

        if (recommendations.isEmpty()) {
            if (isBangla) {
                recommendations.add("সুষম খাদ্য গ্রহণ করুন এবং নিয়মিত শারীরিক পরিশ্রম করুন।");
                recommendations.add("প্রতি বছর নিয়মিত স্বাস্থ্য পরীক্ষা (Health Check-up) করান।");
            } else {
                recommendations.add("Maintain a balanced diet and regular physical exercise.");
                recommendations.add("Schedule routine physical check-ups annually.");
            }
        }

        // Limit to 3 recommendations
        if (recommendations.size() > 3) {
            recommendations = recommendations.subList(0, 3);
        }

        String summary;
        if (isBangla) {
            summary = String.format("উপসর্গ ও শারীরিক পরিমাপের ভিত্তিতে ঝুঁকি বিশ্লেষণ। রক্তচাপ: %s। রক্তের শর্করা: %s। উপসর্গ বিবরণ: %s।",
                    bpVal, glucoseVal, symptoms.isEmpty() ? "কোন বিবরণ পাওয়া যায়নি" : symptoms);
        } else {
            summary = String.format("Risk assessment based on symptoms and vitals. Blood Pressure: %s. Blood Glucose: %s. Symptom reports: %s.",
                    bpVal, glucoseVal, symptoms.isEmpty() ? "None reported" : symptoms);
        }

        VitalsAnalyzeResponse response = VitalsAnalyzeResponse.builder()
                .category(category)
                .severity(severity)
                .bpVal(bpVal)
                .glucoseVal(glucoseVal)
                .summary(summary)
                .recommendations(recommendations)
                .build();

        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private VitalsAnalyzeResponse callGeminiForVitals(String symptomsText, Integer bpSystolic, Integer bpDiastolic, Double glucose, String apiKey) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            String bpInfo = (bpSystolic != null && bpDiastolic != null) ? (bpSystolic + "/" + bpDiastolic + " mmHg") : "Not provided";
            String glucoseInfo = (glucose != null) ? (glucose + " mg/dL") : "Not provided";

            String prompt = "You are an advanced clinical vitals and symptom risk assessment assistant. "
                    + "Analyze the patient's vitals (Blood Pressure: " + bpInfo + ", Fasting Blood Glucose: " + glucoseInfo + ") "
                    + "and their symptom description: \"" + symptomsText + "\".\n\n"
                    + "Instructions:\n"
                    + "1. Detect if the symptoms text is in Bangla. If yes, generate the 'summary' and the 'recommendations' array in clear, professional, yet empathetic Bangla. If in English, generate in English.\n"
                    + "2. Categorize the clinical condition into an English category name (e.g., 'Cardiology Referral Recommended', 'Stage 2 Hypertension (High Risk)', 'Prediabetic Range', 'Normal Baseline').\n"
                    + "3. Set the 'severity' field to one of: 'success' (for normal/healthy), 'warning' (for mild/moderate elevations or warnings), or 'danger' (for high risk, hypertension, diabetic range, or urgent conditions like chest pain).\n"
                    + "4. In the 'summary', write a concise, empathetic explanation of their clinical risk status. Ignore any vulgar, slang, or informal words (e.g., 'শালা') used in the symptoms, and address the medical issue professionally.\n"
                    + "5. In the 'recommendations', provide exactly 3 actionable next-step recommendations.\n\n"
                    + "Return ONLY a raw JSON object matching this structure exactly (no markdown block, no extra text):\n"
                    + "{\"category\": \"Category Name\", \"severity\": \"success|warning|danger\", \"bpVal\": \"" + bpInfo + "\", \"glucoseVal\": \"" + glucoseInfo + "\", \"summary\": \"empathetic risk summary...\", \"recommendations\": [\"Actionable step 1\", \"Actionable step 2\", \"Actionable step 3\"]}";

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
                                Map<String, Object> resMap = objectMapper.readValue(text, Map.class);
                                List<String> recommendations = (List<String>) resMap.get("recommendations");
                                if (recommendations == null) {
                                    recommendations = List.of("Maintain a balanced diet.", "Consult a clinician if issues persist.");
                                }

                                return VitalsAnalyzeResponse.builder()
                                        .category((String) resMap.get("category"))
                                        .severity((String) resMap.get("severity"))
                                        .bpVal((String) resMap.get("bpVal"))
                                        .glucoseVal((String) resMap.get("glucoseVal"))
                                        .summary((String) resMap.get("summary"))
                                        .recommendations(recommendations)
                                        .build();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini Vitals API call failed: " + e.getMessage());
        }
        return null;
    }

    @GetMapping("/hospitals")
    public ResponseEntity<List<Hospital>> getHospitals() {
        return ResponseEntity.ok(hospitalRepository.findAll());
    }

    @PostMapping("/blood-requests")
    public ResponseEntity<BloodRequest> createBloodRequest(@RequestBody Map<String, String> payload) {
        String patientName = payload.get("patientName");
        String bloodGroup = payload.get("bloodGroup");
        String urgency = payload.get("urgency");
        String hospitalName = payload.get("hospital");
        String previousDiseaseHistory = payload.get("previousDiseaseHistory");
        String timeline = payload.getOrDefault("timeline", "Urgent");

        // Find hospital location
        String location = "Dhaka";
        Optional<Hospital> hospOpt = hospitalRepository.findAll().stream()
                .filter(h -> h.getName().equalsIgnoreCase(hospitalName))
                .findFirst();
        if (hospOpt.isPresent()) {
            location = hospOpt.get().getDivision() + ", Bangladesh";
        }

        BloodRequest req = BloodRequest.builder()
                .patientName(patientName)
                .bloodGroup(bloodGroup)
                .urgency(urgency != null ? urgency : "High")
                .hospital(hospitalName)
                .location(location)
                .timeline(timeline)
                .status("Pending")
                .previousDiseaseHistory(previousDiseaseHistory)
                .build();

        return ResponseEntity.ok(bloodRequestRepository.save(req));
    }
}
