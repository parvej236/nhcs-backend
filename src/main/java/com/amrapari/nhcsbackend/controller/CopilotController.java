package com.amrapari.nhcsbackend.controller;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Health Copilot — a personalised assistant grounded in the authenticated
 * patient's complete medical record (profile, chronic diseases, allergies,
 * vitals, prescriptions, lab reports, imaging, appointments).
 *
 * Reuses the same Gemini 2.5 Flash integration pattern already used by
 * {@link PatientController#aiSuggest}.
 */
@RestController
@RequestMapping("/api/v1/patients/copilot")
@RequiredArgsConstructor
public class CopilotController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ---------------------------------------------------------------------
    // 1. Daily Briefing — Health Score + proactive alerts
    // ---------------------------------------------------------------------
    @GetMapping("/briefing")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> briefing(Authentication auth) {
        Patient p = currentPatient(auth);
        if (p == null) return ResponseEntity.notFound().build();

        List<LabReport> labs = labReportRepository.findByPatientIdOrderByDateDesc(p.getId());
        List<Prescription> scripts = prescriptionRepository.findByPatientIdOrderByDateDesc(p.getId());

        int score = computeHealthScore(p, labs);
        String band = scoreBand(score);
        List<Map<String, Object>> alerts = buildAlerts(p, labs);

        // Ask Gemini for a warm, personalised one-line status; fall back to rule-based.
        String statusLine = null;
        String prompt = "You are a friendly personal health assistant for a patient in Bangladesh. "
                + "Based ONLY on the record below, write ONE short encouraging status line (max 22 words, plain English, no medical jargon) "
                + "summarising how the patient is doing today and the single most important thing to focus on. "
                + "Return JSON: {\"statusLine\": \"...\"}\n\nRECORD:\n" + buildContext(p, labs, scripts, false);
        String[] err = new String[1];
        Map<String, Object> g = callGeminiJsonObject(prompt, err);
        if (g != null && g.get("statusLine") != null) statusLine = String.valueOf(g.get("statusLine"));
        if (statusLine == null || statusLine.isBlank()) {
            statusLine = score >= 80 ? "Your health is on a good track — keep up your current routine."
                    : score >= 60 ? "You're doing okay, but a few numbers need attention this week."
                    : "Some of your vitals need care — small consistent steps will help a lot.";
        }

        Map<String, Object> res = new HashMap<>();
        res.put("healthScore", score);
        res.put("scoreBand", band);
        res.put("statusLine", statusLine);
        res.put("alerts", alerts);
        res.put("patientName", p.getFullName());
        res.put("updatedAt", p.getVitalsLastUpdated() != null ? p.getVitalsLastUpdated().toString() : null);
        res.put("aiQuotaExceeded", "quota".equals(err[0]));
        res.put("aiStatus", err[0] == null ? null : aiStatusMessage(err[0]));
        return ResponseEntity.ok(res);
    }

    // ---------------------------------------------------------------------
    // 2. Grounded Chat
    // ---------------------------------------------------------------------
    @PostMapping("/chat")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> chat(Authentication auth, @RequestBody Map<String, Object> body) {
        Patient p = currentPatient(auth);
        if (p == null) return ResponseEntity.notFound().build();

        String message = String.valueOf(body.getOrDefault("message", "")).trim();
        if (message.isEmpty()) return ResponseEntity.badRequest().body(Map.of("reply", "Please type a question."));

        List<LabReport> labs = labReportRepository.findByPatientIdOrderByDateDesc(p.getId());
        List<Prescription> scripts = prescriptionRepository.findByPatientIdOrderByDateDesc(p.getId());

        StringBuilder history = new StringBuilder();
        Object h = body.get("history");
        if (h instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    Object role = m.get("role");
                    Object text = m.get("text");
                    if (role != null && text != null) {
                        history.append("PATIENT".equalsIgnoreCase(role.toString()) ? "Patient: " : "Assistant: ")
                                .append(text).append("\n");
                    }
                }
            }
        }

        String prompt = "You are \"Shonko\", a caring personal AI health companion inside the NHCS app for a patient in Bangladesh. "
                + "You KNOW this patient's full medical record (given below). Answer their question using their OWN data — quote their real numbers, "
                + "medicines and conditions where relevant. Be warm, clear and practical. Reply in the SAME language the patient used "
                + "(Bangla or English), keep it concise (max ~120 words). Use simple everyday words, not clinical jargon. "
                + "You may give lifestyle, diet (Bangladeshi food aware), and medication-adherence guidance, and flag when they should see a doctor. "
                + "NEVER prescribe new prescription drugs or dosages. If the question implies an emergency (chest pain, severe breathlessness, "
                + "stroke signs), tell them to seek emergency care immediately. Do NOT invent facts not in the record.\n\n"
                + "PATIENT RECORD:\n" + buildContext(p, labs, scripts, true) + "\n"
                + (history.length() > 0 ? "CONVERSATION SO FAR:\n" + history + "\n" : "")
                + "Patient's new message: " + message;

        GeminiResult r = callGeminiText(prompt);
        String reply = r.ok() ? r.text : aiStatusMessage(r.errorType);
        Map<String, Object> out = new HashMap<>();
        out.put("reply", reply);
        out.put("quotaExceeded", "quota".equals(r.errorType));
        return ResponseEntity.ok(out);
    }

    // ---------------------------------------------------------------------
    // 3. Medication Safety Check — interactions + allergy conflicts
    // ---------------------------------------------------------------------
    @PostMapping("/medication-check")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> medicationCheck(Authentication auth, @RequestBody(required = false) Map<String, Object> body) {
        Patient p = currentPatient(auth);
        if (p == null) return ResponseEntity.notFound().build();

        List<Prescription> scripts = prescriptionRepository.findByPatientIdOrderByDateDesc(p.getId());
        List<String> meds = new ArrayList<>();
        for (Prescription pr : scripts) {
            if (pr.getMedicines() != null) {
                for (Medicine m : pr.getMedicines()) {
                    meds.add(m.getName() + (m.getDosage() != null ? " " + m.getDosage() : ""));
                }
            }
        }
        // Optional: a new medicine the patient wants to check before taking.
        String newMed = body != null ? String.valueOf(body.getOrDefault("newMedicine", "")).trim() : "";
        if (!newMed.isEmpty() && !"null".equals(newMed)) meds.add(newMed + " (NEW — patient wants to start this)");

        String allergies = p.getAllergies() == null ? "None on file"
                : p.getAllergies().stream().map(a -> a.getAllergen() + " (" + a.getSeverity() + ")").collect(Collectors.joining(", "));

        String prompt = "You are a clinical pharmacology assistant. Given the patient's current medications and known allergies, "
                + "identify: (1) any drug-drug interactions, (2) any drug-allergy conflicts, (3) important timing/food cautions. "
                + "Be accurate and conservative. Return JSON EXACTLY like: "
                + "{\"overallRisk\":\"low|moderate|high\",\"summaryEn\":\"...\",\"summaryBn\":\"...\","
                + "\"interactions\":[{\"severity\":\"low|moderate|high\",\"title\":\"...\",\"detail\":\"...\"}]}. "
                + "If nothing notable, return an empty interactions array and overallRisk low.\n\n"
                + "MEDICATIONS: " + (meds.isEmpty() ? "None recorded" : String.join("; ", meds)) + "\n"
                + "ALLERGIES: " + allergies + "\n"
                + "CONDITIONS: " + conditions(p);

        String[] err = new String[1];
        Map<String, Object> parsed = callGeminiJsonObject(prompt, err);
        if (parsed == null) {
            parsed = new HashMap<>();
            parsed.put("overallRisk", "low");
            parsed.put("summaryEn", aiStatusMessage(err[0]));
            parsed.put("summaryBn", aiStatusMessage(err[0]));
            parsed.put("interactions", new ArrayList<>());
        }
        parsed.put("medications", meds);
        parsed.put("allergies", allergies);
        parsed.put("aiQuotaExceeded", "quota".equals(err[0]));
        return ResponseEntity.ok(parsed);
    }

    // ---------------------------------------------------------------------
    // 4. Risk Radar — cardiovascular + diabetes (deterministic)
    // ---------------------------------------------------------------------
    @GetMapping("/risk")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> risk(Authentication auth) {
        Patient p = currentPatient(auth);
        if (p == null) return ResponseEntity.notFound().build();
        List<LabReport> labs = labReportRepository.findByPatientIdOrderByDateDesc(p.getId());

        Map<String, Object> cardio = cardiovascularRisk(p, labs);
        Map<String, Object> diabetes = diabetesRisk(p, labs);

        Map<String, Object> res = new HashMap<>();
        res.put("cardiovascular", cardio);
        res.put("diabetes", diabetes);
        return ResponseEntity.ok(res);
    }

    // ---------------------------------------------------------------------
    // 5. Explain a report in plain language (Bangla + English)
    // ---------------------------------------------------------------------
    @PostMapping("/explain-report")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> explainReport(Authentication auth, @RequestBody Map<String, Object> body) {
        Patient p = currentPatient(auth);
        if (p == null) return ResponseEntity.notFound().build();

        String type = String.valueOf(body.getOrDefault("reportType", "lab"));
        String reportId = String.valueOf(body.getOrDefault("reportId", ""));

        String reportText;
        if ("imaging".equalsIgnoreCase(type)) {
            ImagingReport ir = imagingReportRepository.findById(reportId).orElse(null);
            if (ir == null || !belongs(ir.getPatient(), p)) return ResponseEntity.notFound().build();
            reportText = "Imaging: " + ir.getType() + " (" + ir.getBodyPart() + ") on " + fmt(ir.getDate())
                    + "\nFindings: " + n(ir.getFindings()) + "\nImpression: " + n(ir.getImpression());
        } else {
            LabReport lr = labReportRepository.findById(reportId).orElse(null);
            if (lr == null || !belongs(lr.getPatient(), p)) return ResponseEntity.notFound().build();
            StringBuilder sb = new StringBuilder("Lab: " + lr.getTestName() + " on " + fmt(lr.getDate()) + "\n");
            if (lr.getResults() != null) {
                for (LabTestResult r : lr.getResults()) {
                    sb.append("- ").append(r.getParameter()).append(": ").append(r.getValue()).append(" ").append(n(r.getUnit()))
                            .append(" (ref ").append(n(r.getReferenceRange())).append(") -> ").append(n(r.getStatus())).append("\n");
                }
            }
            reportText = sb.toString();
        }

        String prompt = "You are explaining a medical report to a patient with no medical background, in Bangladesh. "
                + "Explain what this report means for THIS patient in very simple terms. Consider their conditions: " + conditions(p) + ". "
                + "Return JSON: {\"explanationEn\":\"2-4 simple sentences\",\"explanationBn\":\"same in simple Bangla\","
                + "\"keyPoints\":[\"short actionable point\", \"...\"],\"reassurance\":\"one calm reassuring line\"}.\n\nREPORT:\n" + reportText;

        String[] err = new String[1];
        Map<String, Object> parsed = callGeminiJsonObject(prompt, err);
        if (parsed == null) {
            parsed = new HashMap<>();
            parsed.put("explanationEn", aiStatusMessage(err[0]));
            parsed.put("explanationBn", aiStatusMessage(err[0]));
            parsed.put("keyPoints", new ArrayList<>());
            parsed.put("reassurance", "");
        }
        parsed.put("aiQuotaExceeded", "quota".equals(err[0]));
        return ResponseEntity.ok(parsed);
    }

    // =====================================================================
    // Context building
    // =====================================================================
    private String buildContext(Patient p, List<LabReport> labs, List<Prescription> scripts, boolean full) {
        StringBuilder sb = new StringBuilder();
        Integer age = age(p.getDateOfBirth());
        sb.append("Name: ").append(n(p.getFullName()))
                .append(" | Age: ").append(age != null ? age : "?")
                .append(" | Sex: ").append(n(p.getGender()))
                .append(" | Blood group: ").append(n(p.getBloodGroup())).append("\n");

        sb.append("Chronic conditions: ").append(conditions(p)).append("\n");

        String allergies = (p.getAllergies() == null || p.getAllergies().isEmpty()) ? "None on file"
                : p.getAllergies().stream().map(a -> a.getAllergen() + " (" + a.getSeverity() + " - " + n(a.getReaction()) + ")")
                .collect(Collectors.joining(", "));
        sb.append("Allergies: ").append(allergies).append("\n");

        sb.append("Latest vitals: BP ").append(n(p.getBpSystolic())).append("/").append(n(p.getBpDiastolic()))
                .append(" mmHg, Fasting glucose ").append(n(p.getBloodGlucose())).append(" mg/dL, Heart rate ")
                .append(n(p.getHeartRate())).append(" bpm, Weight ").append(n(p.getWeight())).append(" kg")
                .append(p.getVitalsLastUpdated() != null ? " (as of " + fmt(p.getVitalsLastUpdated()) + ")" : "").append("\n");

        // Current medicines
        List<String> meds = new ArrayList<>();
        for (Prescription pr : scripts) {
            if (pr.getMedicines() != null) {
                for (Medicine m : pr.getMedicines()) {
                    meds.add(m.getName() + " " + n(m.getDosage()) + " — " + n(m.getInstruction()));
                }
            }
        }
        sb.append("Current medications: ").append(meds.isEmpty() ? "None recorded" : String.join("; ", meds)).append("\n");

        // Recent lab results (abnormal-first)
        if (labs != null && !labs.isEmpty()) {
            sb.append("Recent lab results:\n");
            int shown = 0;
            for (LabReport lr : labs) {
                if (shown >= (full ? 3 : 2)) break;
                sb.append("  ").append(lr.getTestName()).append(" (").append(fmt(lr.getDate())).append("): ");
                if (lr.getResults() != null) {
                    sb.append(lr.getResults().stream()
                            .map(r -> r.getParameter() + " " + r.getValue() + n(r.getUnit()) + " [" + n(r.getStatus()) + "]")
                            .collect(Collectors.joining(", ")));
                }
                sb.append("\n");
                shown++;
            }
        }

        if (full) {
            // Upcoming appointments
            List<Appointment> apps = appointmentRepository.findByPatientIdOrderByDateDesc(p.getId());
            List<Appointment> upcoming = apps.stream()
                    .filter(a -> a.getDate() != null && !a.getDate().isBefore(LocalDate.now()))
                    .collect(Collectors.toList());
            if (!upcoming.isEmpty()) {
                sb.append("Upcoming appointments: ");
                sb.append(upcoming.stream().limit(3).map(a ->
                        (a.getDoctor() != null ? a.getDoctor().getFullName() : "Doctor") + " on " + a.getDate()
                                + " (" + n(a.getTimeSlot()) + ")").collect(Collectors.joining("; ")));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String conditions(Patient p) {
        if (p.getChronicDiseases() == null || p.getChronicDiseases().isEmpty()) return "None on file";
        return p.getChronicDiseases().stream()
                .map(c -> c.getDiseaseName() + " (" + n(c.getStatus()) + ")")
                .collect(Collectors.joining(", "));
    }

    // =====================================================================
    // Deterministic scoring
    // =====================================================================
    private int computeHealthScore(Patient p, List<LabReport> labs) {
        int score = 100;
        double glucose = num(p.getBloodGlucose());
        if (glucose > 180) score -= 25; else if (glucose > 140) score -= 18; else if (glucose > 125) score -= 12;
        else if (glucose > 0 && glucose < 70) score -= 10;

        double sys = num(p.getBpSystolic());
        if (sys > 160) score -= 20; else if (sys > 140) score -= 12; else if (sys > 130) score -= 6;
        double dia = num(p.getBpDiastolic());
        if (dia > 100) score -= 12; else if (dia > 90) score -= 8;

        int chronic = p.getChronicDiseases() == null ? 0 : p.getChronicDiseases().size();
        score -= Math.min(chronic * 6, 18);

        // Abnormal lipid markers from most recent labs
        if (labs != null) {
            for (LabReport lr : labs) {
                if (lr.getResults() == null) continue;
                for (LabTestResult r : lr.getResults()) {
                    String param = n(r.getParameter()).toLowerCase();
                    String status = n(r.getStatus()).toLowerCase();
                    if (!status.contains("high") && !status.contains("low") && !status.contains("abnormal")) continue;
                    if (param.contains("ldl")) score -= 6;
                    else if (param.contains("cholesterol")) score -= 4;
                    else if (param.contains("triglyceride")) score -= 4;
                }
                break; // only most recent report
            }
        }
        return Math.max(5, Math.min(100, score));
    }

    private String scoreBand(int s) {
        if (s >= 85) return "Excellent";
        if (s >= 70) return "Good";
        if (s >= 50) return "Fair";
        return "Needs Attention";
    }

    private List<Map<String, Object>> buildAlerts(Patient p, List<LabReport> labs) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        double glucose = num(p.getBloodGlucose());
        if (glucose > 180) alerts.add(alert("high", "High blood glucose", "Fasting glucose is " + p.getBloodGlucose() + " mg/dL (target 70–100). Review diet and Metformin adherence."));
        else if (glucose > 125) alerts.add(alert("moderate", "Elevated blood glucose", "Fasting glucose is " + p.getBloodGlucose() + " mg/dL, above the normal range."));

        double sys = num(p.getBpSystolic());
        double dia = num(p.getBpDiastolic());
        if (sys > 140 || dia > 90) alerts.add(alert("moderate", "Blood pressure above target", "Latest BP is " + p.getBpSystolic() + "/" + p.getBpDiastolic() + " mmHg. Keep monitoring and limit salt."));

        // Severe allergies as a persistent safety flag
        if (p.getAllergies() != null) {
            for (PatientAllergy a : p.getAllergies()) {
                if ("severe".equalsIgnoreCase(n(a.getSeverity()))) {
                    alerts.add(alert("high", "Severe allergy on file: " + a.getAllergen(),
                            "Reaction: " + n(a.getReaction()) + ". Always tell any doctor before new prescriptions."));
                }
            }
        }

        // Abnormal lipids
        if (labs != null && !labs.isEmpty()) {
            LabReport lr = labs.get(0);
            if (lr.getResults() != null) {
                for (LabTestResult r : lr.getResults()) {
                    if (n(r.getStatus()).toLowerCase().contains("high") && n(r.getParameter()).toLowerCase().contains("ldl")) {
                        alerts.add(alert("moderate", "High LDL cholesterol", r.getParameter() + " is " + r.getValue() + " " + n(r.getUnit()) + " (target " + n(r.getReferenceRange()) + ")."));
                    }
                }
            }
        }
        return alerts;
    }

    private Map<String, Object> cardiovascularRisk(Patient p, List<LabReport> labs) {
        int risk = 0;
        List<String> factors = new ArrayList<>();
        Integer age = age(p.getDateOfBirth());
        if (age != null && age >= 45) { risk += 15; factors.add("Age " + age); }
        double sys = num(p.getBpSystolic());
        if (sys > 140) { risk += 25; factors.add("High blood pressure (" + p.getBpSystolic() + "/" + p.getBpDiastolic() + ")"); }
        else if (sys > 130) { risk += 12; factors.add("Borderline blood pressure"); }
        if (hasCondition(p, "diabetes")) { risk += 20; factors.add("Diabetes"); }
        if (labAbnormal(labs, "ldl")) { risk += 18; factors.add("High LDL cholesterol"); }
        if (labAbnormal(labs, "cholesterol")) { risk += 8; factors.add("High total cholesterol"); }
        if (labAbnormal(labs, "triglyceride")) { risk += 6; factors.add("High triglycerides"); }
        risk = Math.min(risk, 98);
        return riskMap("Cardiovascular (heart & stroke)", risk, factors,
                "Manage BP and cholesterol, stay active 30 min/day, reduce salt and fried food.");
    }

    private Map<String, Object> diabetesRisk(Patient p, List<LabReport> labs) {
        int risk = 0;
        List<String> factors = new ArrayList<>();
        boolean diabetic = hasCondition(p, "diabetes");
        double glucose = num(p.getBloodGlucose());
        if (diabetic) { risk += 30; factors.add("Existing Type 2 Diabetes"); }
        if (glucose > 180) { risk += 35; factors.add("Very high fasting glucose (" + p.getBloodGlucose() + ")"); }
        else if (glucose > 140) { risk += 22; factors.add("High fasting glucose (" + p.getBloodGlucose() + ")"); }
        else if (glucose > 125) { risk += 12; factors.add("Elevated fasting glucose"); }
        Integer age = age(p.getDateOfBirth());
        if (age != null && age >= 45) { risk += 8; factors.add("Age " + age); }
        risk = Math.min(risk, 98);
        return riskMap(diabetic ? "Diabetes complications" : "Type 2 Diabetes", risk, factors,
                "Keep fasting glucose near 100 mg/dL, take medicines on time, prefer low-glycaemic Bangladeshi foods (less white rice).");
    }

    private Map<String, Object> riskMap(String title, int risk, List<String> factors, String advice) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("riskPercent", risk);
        m.put("level", risk >= 70 ? "Very High" : risk >= 45 ? "High" : risk >= 25 ? "Moderate" : "Low");
        m.put("factors", factors);
        m.put("advice", advice);
        return m;
    }

    // =====================================================================
    // Small helpers
    // =====================================================================
    private Map<String, Object> alert(String severity, String title, String detail) {
        Map<String, Object> m = new HashMap<>();
        m.put("severity", severity);
        m.put("title", title);
        m.put("detail", detail);
        return m;
    }

    private boolean hasCondition(Patient p, String keyword) {
        if (p.getChronicDiseases() == null) return false;
        return p.getChronicDiseases().stream().anyMatch(c -> n(c.getDiseaseName()).toLowerCase().contains(keyword));
    }

    private boolean labAbnormal(List<LabReport> labs, String paramKeyword) {
        if (labs == null) return false;
        for (LabReport lr : labs) {
            if (lr.getResults() == null) continue;
            for (LabTestResult r : lr.getResults()) {
                if (n(r.getParameter()).toLowerCase().contains(paramKeyword)
                        && n(r.getStatus()).toLowerCase().contains("high")) return true;
            }
            break; // most recent only
        }
        return false;
    }

    private Patient currentPatient(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .flatMap(user -> patientRepository.findByUserId(user.getId()))
                .orElse(null);
    }

    private boolean belongs(Patient owner, Patient p) {
        return owner != null && owner.getId() != null && owner.getId().equals(p.getId());
    }

    private Integer age(LocalDate dob) {
        if (dob == null) return null;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private double num(String s) {
        if (s == null) return 0;
        try {
            return Double.parseDouble(s.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String n(String s) {
        return s == null ? "" : s;
    }

    private String fmt(java.time.LocalDateTime dt) {
        return dt == null ? "" : dt.toLocalDate().format(D);
    }

    // =====================================================================
    // Gemini plumbing (mirrors PatientController.callGemini)
    // =====================================================================

    /** Outcome of a Gemini call: either text (success) or an errorType. */
    private static class GeminiResult {
        final String text;      // non-null on success
        final String errorType; // null on success; "quota" | "auth" | "config" | "error"
        GeminiResult(String text, String errorType) { this.text = text; this.errorType = errorType; }
        boolean ok() { return text != null && errorType == null; }
    }

    private String resolveKey() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) apiKey = geminiApiKey;
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_")) return null;
        return apiKey;
    }

    /**
     * A user-facing, bilingual message explaining why the AI couldn't answer.
     * "quota" specifically means the free-tier daily limit is exhausted.
     */
    private String aiStatusMessage(String errorType) {
        if ("quota".equals(errorType)) {
            return "⚠️ আজকের ফ্রি AI সীমা শেষ হয়ে গেছে — AI সহকারী আগামীকাল আবার কাজ করবে। "
                    + "(আপনার হেলথ স্কোর, অ্যালার্ট ও রিস্ক ঠিকঠাক আছে।)\n"
                    + "Today's free AI limit has been used up. The assistant will be back tomorrow.";
        }
        if ("auth".equals(errorType)) {
            return "⚠️ AI পরিষেবার কী (API key) অকার্যকর হয়ে গেছে। অনুগ্রহ করে Gemini API key আপডেট করুন।\n"
                    + "The AI service key is invalid — please update the Gemini API key.";
        }
        if ("config".equals(errorType)) {
            return "⚠️ AI এখনও কনফিগার করা হয়নি (কোনো API key সেট করা নেই)।\n"
                    + "AI is not configured — no API key is set.";
        }
        return "দুঃখিত, এই মুহূর্তে AI সহকারীর সাথে সংযোগ করা যায়নি। একটু পরে আবার চেষ্টা করুন।\n"
                + "Sorry, couldn't reach the AI assistant right now. Please try again in a moment.";
    }

    /** Raw text generation (no JSON constraint). */
    private GeminiResult callGeminiText(String prompt) {
        GeminiResult r = callGeminiRaw(prompt, false);
        return r.ok() ? new GeminiResult(r.text.trim(), null) : r;
    }

    /** JSON generation, parsed into a flat String map (values coerced to String). */
    private Map<String, String> callGeminiJson(String prompt) {
        GeminiResult r = callGeminiRaw(prompt, true);
        if (!r.ok()) return null;
        try {
            Map<?, ?> m = objectMapper.readValue(stripFences(r.text), Map.class);
            Map<String, String> out = new HashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue() == null ? null : String.valueOf(e.getValue()));
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON generation, parsed into a full object map (nested lists/maps preserved).
     * On failure returns null but records the errorType into out[0].
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callGeminiJsonObject(String prompt, String[] errorOut) {
        GeminiResult r = callGeminiRaw(prompt, true);
        if (errorOut != null && errorOut.length > 0) errorOut[0] = r.errorType;
        if (!r.ok()) return null;
        try {
            return objectMapper.readValue(stripFences(r.text), Map.class);
        } catch (Exception e) {
            if (errorOut != null && errorOut.length > 0) errorOut[0] = "error";
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private GeminiResult callGeminiRaw(String prompt, boolean jsonMode) {
        String apiKey = resolveKey();
        if (apiKey == null) return new GeminiResult(null, "config");
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(6))
                    .build();

            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            Map<String, Object> contentItem = new HashMap<>();
            contentItem.put("parts", List.of(part));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(contentItem));
            if (jsonMode) {
                Map<String, Object> generationConfig = new HashMap<>();
                generationConfig.put("responseMimeType", "application/json");
                body.put("generationConfig", generationConfig);
            }

            String requestBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code == 200) {
                Map<String, Object> json = objectMapper.readValue(response.body(), Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) json.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return new GeminiResult((String) parts.get(0).get("text"), null);
                        }
                    }
                }
                return new GeminiResult(null, "error");
            }
            System.err.println("Gemini API error " + code + ": " + response.body());
            if (code == 429) return new GeminiResult(null, "quota");           // free-tier limit reached
            if (code == 401 || code == 403) return new GeminiResult(null, "auth"); // bad/leaked key
            return new GeminiResult(null, "error");
        } catch (Exception e) {
            System.err.println("Gemini call failed: " + e.getMessage());
            return new GeminiResult(null, "error");
        }
    }

    private String stripFences(String text) {
        text = text.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastBackticks = text.lastIndexOf("```");
            if (firstNewline != -1 && lastBackticks > firstNewline) {
                text = text.substring(firstNewline, lastBackticks).trim();
            }
        }
        return text;
    }
}
