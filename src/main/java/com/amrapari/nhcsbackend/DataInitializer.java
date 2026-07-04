package com.amrapari.nhcsbackend;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PatientChronicDiseaseRepository chronicDiseaseRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabReportRepository labReportRepository;
    private final ImagingReportRepository imagingReportRepository;
    private final PasswordEncoder passwordEncoder;
    private final BloodRequestRepository bloodRequestRepository;
    private final BloodDonorRepository bloodDonorRepository;

    @Override
    public void run(String... args) throws Exception {
        createMockHospitalsIfEmpty();
        createMockDoctorsIfEmpty();
        createMockPatientIfEmpty();
        createLoginAccountsIfEmpty();
        createMockBloodRequestsIfEmpty();
        createJudgeDataIfEmpty();
    }

    /**
     * Seeds the demo login accounts for the elevated roles. Patient
     * self-registers (see AuthService); Doctor / Hospital / Admin are never
     * self-created, so they are seeded here. The doctor account is linked to an
     * existing seeded Doctor profile so it resolves the doctor portal on login.
     *
     * Dev demo credentials (do NOT ship to production): doctor / password123
     * (ROLE_DOCTOR, linked to Dr. Ahmed Chowdhury) hospital / password123
     * (ROLE_HOSPITAL) The existing patient account is seeded in
     * createMockPatientIfEmpty(): patient / password123 (ROLE_PATIENT)
     */
    private void createLoginAccountsIfEmpty() {
        if (userRepository.findByUsername("doctor").isEmpty()) {
            User doctorUser = User.builder()
                    .username("doctor")
                    .email("doctor@nhcs.gov")
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(Role.DOCTOR)))
                    .build();
            userRepository.save(doctorUser);

            // Link this login account to a seeded doctor profile that has no user yet
            // so the doctor portal resolves a profile for the authenticated user.
            // If every profile is already linked, create a fresh one for the login.
            Doctor unlinked = doctorRepository.findAll().stream()
                    .filter(d -> d.getUser() == null)
                    .findFirst()
                    .orElse(null);
            if (unlinked != null) {
                unlinked.setUser(doctorUser);
                doctorRepository.save(unlinked);
            } else {
                doctorRepository.save(Doctor.builder()
                        .user(doctorUser)
                        .fullName("Dr. Demo Physician")
                        .specialization("General Medicine")
                        .licenseNumber("MBBS-DEMO")
                        .hospitalAffiliation("Dhaka Central Hospital")
                        .rating(4.8)
                        .experienceYears(10)
                        .consultationFee(800)
                        .build());
            }
        }

        if (userRepository.findByUsername("hospital").isEmpty()) {
            User hospitalUser = User.builder()
                    .username("hospital")
                    .email("hospital@nhcs.gov")
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(Role.HOSPITAL)))
                    .build();
            userRepository.save(hospitalUser);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@nhcs.gov")
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(Role.ADMIN)))
                    .build();
            userRepository.save(adminUser);
        }
    }

    private void createMockDoctorsIfEmpty() {
        if (doctorRepository.count() == 0) {
            doctorRepository.save(Doctor.builder()
                    .fullName("Dr. Ahmed Chowdhury")
                    .specialization("Endocrinology & Diabetology")
                    .licenseNumber("MBBS-1001")
                    .contactNumber("+8801711111111")
                    .hospitalAffiliation("Dhaka Central Hospital")
                    .rating(4.9)
                    .experienceYears(16)
                    .consultationFee(1000)
                    .imageUrl("https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop")
                    .build());

            doctorRepository.save(Doctor.builder()
                    .fullName("Dr. Fahmida Rahman")
                    .specialization("Cardiology")
                    .licenseNumber("MBBS-1002")
                    .contactNumber("+8801722222222")
                    .hospitalAffiliation("National Medical Center")
                    .rating(4.8)
                    .experienceYears(12)
                    .consultationFee(1200)
                    .imageUrl("https://images.unsplash.com/photo-1594824813573-246434de83fb?q=80&w=200&auto=format&fit=crop")
                    .build());

            doctorRepository.save(Doctor.builder()
                    .fullName("Dr. Tanveer Hassan")
                    .specialization("General Medicine")
                    .licenseNumber("MBBS-1003")
                    .contactNumber("+8801733333333")
                    .hospitalAffiliation("Ibn Sina Medical College")
                    .rating(4.7)
                    .experienceYears(8)
                    .consultationFee(800)
                    .imageUrl("https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200&auto=format&fit=crop")
                    .build());

            doctorRepository.save(Doctor.builder()
                    .fullName("Dr. Nusrat Jahan Tania")
                    .specialization("Gynaecology & Obstetrics")
                    .licenseNumber("MBBS-1004")
                    .contactNumber("+8801744444444")
                    .hospitalAffiliation("Dhaka Medical College Hospital")
                    .rating(4.9)
                    .experienceYears(14)
                    .consultationFee(1000)
                    .imageUrl("https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=200&auto=format&fit=crop")
                    .build());
        }
    }

    private void createMockPatientIfEmpty() {
        if (userRepository.findByUsername("patient").isPresent()) {
            User existing = userRepository.findByUsername("patient").get();
            if (patientRepository.findByUserId(existing.getId()).isEmpty() || appointmentRepository.count() == 0) {
                patientRepository.findByUserId(existing.getId()).ifPresent(p -> {
                    appointmentRepository.deleteAll(appointmentRepository.findByPatientId(p.getId()));
                    prescriptionRepository.deleteAll(prescriptionRepository.findByPatientId(p.getId()));
                    labReportRepository.deleteAll(labReportRepository.findByPatientId(p.getId()));
                    imagingReportRepository.deleteAll(imagingReportRepository.findByPatientId(p.getId()));
                    patientRepository.delete(p);
                });
                userRepository.delete(existing);

                appointmentRepository.flush();
                prescriptionRepository.flush();
                labReportRepository.flush();
                imagingReportRepository.flush();
                patientRepository.flush();
                userRepository.flush();
            }
        }

        if (userRepository.findByUsername("patient").isEmpty()) {
            User user = User.builder()
                    .username("patient")
                    .email("patient@nhcs.gov")
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(Role.PATIENT)))
                    .build();
            userRepository.save(user);

            Patient patient = Patient.builder()
                    .user(user)
                    .fullName("Rahim Islam")
                    .dateOfBirth(LocalDate.of(1984, 8, 15))
                    .gender("Male")
                    .bloodGroup("O+")
                    .nationalId("8210398457")
                    .contactNumber("+880 1712-345678")
                    .address("House 45, Road 12, Dhanmondi, Dhaka 1209")
                    .occupation("Software Engineer")
                    .maritalStatus("Married")
                    .presentAddress("House 45, Road 12, Dhanmondi, Dhaka 1209")
                    .permanentAddress("Village Purbadhala, Netrokona, Mymensingh")
                    .emergencyContactName("Nusrat Jahan")
                    .emergencyContactRelation("Spouse")
                    .emergencyContactPhone("+880 1911-987654")
                    .bpSystolic("130")
                    .bpDiastolic("85")
                    .bloodGlucose("180")
                    .heartRate("78")
                    .weight("75")
                    .vitalsLastUpdated(LocalDateTime.now().minusMinutes(45))
                    .build();
            patientRepository.save(patient);

            allergyRepository.save(PatientAllergy.builder()
                    .patient(patient)
                    .allergen("Penicillin")
                    .severity("Severe")
                    .reaction("Anaphylaxis, hives")
                    .build());

            allergyRepository.save(PatientAllergy.builder()
                    .patient(patient)
                    .allergen("Dust Mites")
                    .severity("Mild")
                    .reaction("Sneezing, nasal congestion")
                    .build());

            chronicDiseaseRepository.save(PatientChronicDisease.builder()
                    .patient(patient)
                    .diseaseName("Type 2 Diabetes")
                    .status("Active")
                    .diagnosedDate(LocalDate.of(2021, 3, 12))
                    .build());

            chronicDiseaseRepository.save(PatientChronicDisease.builder()
                    .patient(patient)
                    .diseaseName("Hypertension")
                    .status("Managed")
                    .diagnosedDate(LocalDate.of(2023, 6, 20))
                    .build());

            List<Doctor> doctors = doctorRepository.findAll();
            Doctor drAhmed = doctors.stream().filter(d -> d.getFullName().contains("Ahmed")).findFirst().orElse(null);
            Doctor drFahmida = doctors.stream().filter(d -> d.getFullName().contains("Fahmida")).findFirst().orElse(null);

            if (drAhmed != null) {
                appointmentRepository.save(Appointment.builder()
                        .id("APP-101")
                        .patient(patient)
                        .doctor(drAhmed)
                        .date(LocalDate.now().plusDays(1))
                        .timeSlot("10:30 AM")
                        .queueNumber("Q-07")
                        .status("Upcoming")
                        .approvalStatus("PENDING")
                        .arrivalStatus("AWAITING")
                        .hospitalName("Dhaka Central Hospital")
                        .build());

                appointmentRepository.save(Appointment.builder()
                        .id("APP-103")
                        .patient(patient)
                        .doctor(drAhmed)
                        .date(LocalDate.now().minusDays(20))
                        .timeSlot("09:00 AM")
                        .queueNumber("Q-03")
                        .status("Past")
                        .approvalStatus("APPROVED")
                        .arrivalStatus("COMPLETED")
                        .hospitalName("Dhaka Central Hospital")
                        .build());
            }

            if (drFahmida != null) {
                appointmentRepository.save(Appointment.builder()
                        .id("APP-102")
                        .patient(patient)
                        .doctor(drFahmida)
                        .date(LocalDate.now().plusDays(4))
                        .timeSlot("04:30 PM")
                        .queueNumber("Q-14")
                        .status("Upcoming")
                        .approvalStatus("APPROVED")
                        .arrivalStatus("AWAITING")
                        .hospitalName("National Medical Center")
                        .build());
            }

            Prescription p2 = Prescription.builder()
                    .id("PR-102")
                    .patient(patient)
                    .date(LocalDateTime.now().minusDays(27))
                    .doctorName("Dr. Ahmed Chowdhury")
                    .doctorSpecialization("Endocrinology & Diabetology")
                    .hospitalName("Dhaka Central Hospital")
                    .diagnosis("Type 2 Diabetes Mellitus & Hypertension")
                    .clinicalNotes("Check fasting glucose daily. Maintain a low carbohydrate diet and exercise at least 30 minutes daily. Follow up in 30 days with a fresh HbA1c report.")
                    .followUpDate("2026-06-30")
                    .build();
            p2.getMedicines().add(Medicine.builder().prescription(p2).name("Metformin Hydrochloride").dosage("500mg").instruction("1 tablet twice daily after meals (morning and night)").duration("30 days").build());
            p2.getMedicines().add(Medicine.builder().prescription(p2).name("Amlodipine Besylate").dosage("5mg").instruction("1 tablet once daily in the morning").duration("30 days").build());
            p2.getMedicines().add(Medicine.builder().prescription(p2).name("Atorvastatin Calcium").dosage("10mg").instruction("1 tablet once daily before bed").duration("30 days").build());
            prescriptionRepository.save(p2);

            Prescription p1 = Prescription.builder()
                    .id("PR-101")
                    .patient(patient)
                    .date(LocalDateTime.now().minusDays(220))
                    .doctorName("Dr. S. M. Zafar")
                    .doctorSpecialization("Gastroenterology")
                    .hospitalName("Dhaka Central Hospital")
                    .diagnosis("Acute Gastritis & Acid Reflux")
                    .clinicalNotes("Avoid spicy and oily foods. Avoid sleeping immediately after taking meals. Remain hydrated.")
                    .build();
            p1.getMedicines().add(Medicine.builder().prescription(p1).name("Esomeprazole Magnesium").dosage("20mg").instruction("1 capsule daily 30 minutes before breakfast").duration("14 days").build());
            p1.getMedicines().add(Medicine.builder().prescription(p1).name("Domperidone").dosage("10mg").instruction("1 tablet three times daily 15 minutes before meals").duration("7 days").build());
            prescriptionRepository.save(p1);

            LabReport lr = LabReport.builder()
                    .id("LR-201")
                    .patient(patient)
                    .testName("Fasting Blood Glucose (FBG) & Lipid Profile")
                    .category("Biochemistry")
                    .date(LocalDateTime.now().minusDays(28))
                    .hospitalName("Dhaka Central Hospital Lab")
                    .doctorName("Dr. Ahmed Chowdhury")
                    .status("Published")
                    .aiInterpretation("WARNING: Your Fasting Blood Sugar is significantly high (180 mg/dL), indicating poor glycaemic control. Total Cholesterol and Bad Cholesterol (LDL) are also moderately elevated. This combination increases long-term cardiovascular risks. Medication compliance and dietary check are urgent.")
                    .build();
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("Fasting Blood Sugar").value("180").unit("mg/dL").referenceRange("70 - 100").status("High").build());
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("Total Cholesterol").value("210").unit("mg/dL").referenceRange("< 200").status("High").build());
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("HDL (Good Cholesterol)").value("42").unit("mg/dL").referenceRange("> 40").status("Normal").build());
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("LDL (Bad Cholesterol)").value("135").unit("mg/dL").referenceRange("< 100").status("High").build());
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("Triglycerides").value("165").unit("mg/dL").referenceRange("< 150").status("High").build());
            labReportRepository.save(lr);

            imagingReportRepository.save(ImagingReport.builder()
                    .id("IM-301")
                    .patient(patient)
                    .type("X-Ray")
                    .bodyPart("Chest")
                    .date(LocalDateTime.now().minusDays(100))
                    .hospitalName("National Medical Center")
                    .doctorName("Dr. Fahmida Rahman")
                    .imageUrl("https://images.unsplash.com/photo-1559757175-5700dde675bc?q=80&w=600&auto=format&fit=crop")
                    .findings("Lungs are clear bilaterally. No focal consolidation, effusion, or pneumothorax is identified. Cardiomediastinal shadow is within normal limits. Bony structures and soft tissues are unremarkable.")
                    .impression("Normal chest radiograph. No active cardiopulmonary disease.")
                    .build());
        }
    }

    private void createMockHospitalsIfEmpty() {
        if (hospitalRepository.count() == 0) {
            hospitalRepository.save(Hospital.builder()
                    .facilityId("FAC-1001")
                    .name("Dhaka Medical College Hospital")
                    .division("Dhaka")
                    .classification("Public")
                    .totalBeds(2600)
                    .occupiedBeds(2450)
                    .complianceScore(88)
                    .status("Active")
                    .build());

            hospitalRepository.save(Hospital.builder()
                    .facilityId("FAC-1002")
                    .name("Square Hospitals Ltd.")
                    .division("Dhaka")
                    .classification("Private")
                    .totalBeds(400)
                    .occupiedBeds(320)
                    .complianceScore(98)
                    .status("Active")
                    .build());

            hospitalRepository.save(Hospital.builder()
                    .facilityId("FAC-1003")
                    .name("Chittagong Medical College")
                    .division("Chattogram")
                    .classification("Public")
                    .totalBeds(1313)
                    .occupiedBeds(1200)
                    .complianceScore(72)
                    .status("Under Review")
                    .build());

            hospitalRepository.save(Hospital.builder()
                    .facilityId("FAC-1004")
                    .name("Sylhet MAG Osmani Medical")
                    .division("Sylhet")
                    .classification("Public")
                    .totalBeds(900)
                    .occupiedBeds(850)
                    .complianceScore(85)
                    .status("Active")
                    .build());
        }
    }

    private void createMockBloodRequestsIfEmpty() {
        if (bloodRequestRepository.count() == 0) {
            bloodRequestRepository.save(BloodRequest.builder()
                    .patientName("Karim Uddin")
                    .bloodGroup("O+")
                    .urgency("High")
                    .hospital("Dhaka Central Hospital")
                    .location("Dhanmondi, Dhaka")
                    .timeline("Within 2 hours")
                    .status("Pending")
                    .build());

            bloodRequestRepository.save(BloodRequest.builder()
                    .patientName("Sultana Begum")
                    .bloodGroup("O+")
                    .urgency("Medium")
                    .hospital("National Medical Center")
                    .location("Dhaka")
                    .timeline("By tomorrow morning")
                    .status("Pending")
                    .build());

            bloodRequestRepository.save(BloodRequest.builder()
                    .patientName("Fahim Ahmed")
                    .bloodGroup("O+")
                    .urgency("High")
                    .hospital("Chittagong General Hospital")
                    .location("Chittagong")
                    .timeline("Urgent")
                    .status("Pending")
                    .build());
        }
    }

    private void createJudgeDataIfEmpty() {
        if (userRepository.findByUsername("patient_judge").isEmpty()) {
            User patientUser = User.builder()
                    .username("patient_judge")
                    .email("patient_judge@nhcs.gov")
                    .password(passwordEncoder.encode("password123"))
                    .roles(new HashSet<>(Set.of(Role.PATIENT)))
                    .build();
            userRepository.save(patientUser);

            Patient patientJudge = Patient.builder()
                    .user(patientUser)
                    .fullName("Laila Khan")
                    .dateOfBirth(LocalDate.of(1982, 12, 5))
                    .gender("Female")
                    .bloodGroup("O+")
                    .nationalId("8210398457")
                    .contactNumber("+880 1712-345678")
                    .address("House 45, Road 12, Dhanmondi, Dhaka 1209")
                    .occupation("Service Holder")
                    .maritalStatus("Married")
                    .presentAddress("House 45, Road 12, Dhanmondi, Dhaka 1209")
                    .permanentAddress("Village Purbadhala, Netrokona, Mymensingh")
                    .emergencyContactName("Nusrat Jahan")
                    .emergencyContactRelation("Spouse")
                    .emergencyContactPhone("+880 1911-987654")
                    .bpSystolic("165")
                    .bpDiastolic("100")
                    .bloodGlucose("180")
                    .heartRate("110")
                    .weight("75")
                    .vitalsLastUpdated(LocalDateTime.now())
                    .build();
            patientRepository.save(patientJudge);

            allergyRepository.save(PatientAllergy.builder()
                    .patient(patientJudge)
                    .allergen("Penicillin")
                    .severity("Severe")
                    .reaction("Anaphylaxis, hives")
                    .build());

            chronicDiseaseRepository.save(PatientChronicDisease.builder()
                    .patient(patientJudge)
                    .diseaseName("Hypertension")
                    .status("Active")
                    .diagnosedDate(LocalDate.of(2023, 6, 20))
                    .build());

            User doctorUser = userRepository.findByUsername("doctor_judge").orElseGet(() -> {
                User du = User.builder()
                        .username("doctor_judge")
                        .email("doctor_judge@nhcs.gov")
                        .password(passwordEncoder.encode("password123"))
                        .roles(new HashSet<>(Set.of(Role.DOCTOR)))
                        .build();
                return userRepository.save(du);
            });

            Doctor docJudge = doctorRepository.save(Doctor.builder()
                    .user(doctorUser)
                    .fullName("Dr. Rahim Chowdhury")
                    .specialization("Cardiology")
                    .licenseNumber("MBBS-JUDGE")
                    .contactNumber("+8801722222222")
                    .hospitalAffiliation("Dhaka Medical College Hospital")
                    .rating(4.99)
                    .experienceYears(3)
                    .consultationFee(600)
                    .imageUrl("https://images.unsplash.com/photo-1622253692010-333f2da6031d?q=80&w=200&auto=format&fit=crop")
                    .build());

            User hospitalUser = userRepository.findByUsername("hospital_judge").orElseGet(() -> {
                User hu = User.builder()
                        .username("hospital_judge")
                        .email("hospital_judge@nhcs.gov")
                        .password(passwordEncoder.encode("password123"))
                        .roles(new HashSet<>(Set.of(Role.HOSPITAL)))
                        .build();
                return userRepository.save(hu);
            });

            appointmentRepository.save(Appointment.builder()
                    .id("APP-JUDGE-1")
                    .patient(patientJudge)
                    .doctor(docJudge)
                    .date(LocalDate.now().plusDays(1))
                    .timeSlot("10:30 AM")
                    .queueNumber("Q-01")
                    .status("Upcoming")
                    .approvalStatus("PENDING")
                    .arrivalStatus("AWAITING")
                    .hospitalName("Dhaka Medical College Hospital")
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .id("APP-JUDGE-2")
                    .patient(patientJudge)
                    .doctor(docJudge)
                    .date(LocalDate.now().minusDays(3))
                    .timeSlot("09:00 AM")
                    .queueNumber("Q-03")
                    .status("Past")
                    .approvalStatus("APPROVED")
                    .arrivalStatus("COMPLETED")
                    .hospitalName("Dhaka Medical College Hospital")
                    .build());

            Prescription pr = Prescription.builder()
                    .id("PR-JUDGE-1")
                    .patient(patientJudge)
                    .date(LocalDateTime.now().minusDays(3))
                    .doctorName("Dr. Rahim Chowdhury")
                    .doctorSpecialization("Cardiology")
                    .hospitalName("Dhaka Medical College Hospital")
                    .diagnosis("Hypertensive Emergency & Tachycardia")
                    .clinicalNotes("Avoid strenuous physical work. Follow up in 30 days.")
                    .followUpDate(LocalDate.now().plusDays(27).toString())
                    .build();
            pr.getMedicines().add(Medicine.builder().prescription(pr).name("Amlodipine").dosage("5mg").instruction("1 tablet once daily").duration("30 days").build());
            prescriptionRepository.save(pr);

            LabReport lr = LabReport.builder()
                    .id("LR-JUDGE-1")
                    .patient(patientJudge)
                    .testName("Cardiac Enzymes & Troponin-I")
                    .category("Cardiology Lab")
                    .date(LocalDateTime.now().minusDays(3))
                    .hospitalName("Dhaka Medical College Hospital Lab")
                    .doctorName("Dr. Rahim Chowdhury")
                    .status("Published")
                    .aiInterpretation("WARNING: Troponin-I levels are slightly elevated (0.04 ng/mL). BP is critical. Urgent cardiology referral suggested.")
                    .build();
            lr.getResults().add(LabTestResult.builder().labReport(lr).parameter("Troponin-I").value("0.04").unit("ng/mL").referenceRange("< 0.01").status("High").build());
            labReportRepository.save(lr);

            imagingReportRepository.save(ImagingReport.builder()
                    .id("IM-JUDGE-1")
                    .patient(patientJudge)
                    .type("Echocardiogram (ECG)")
                    .bodyPart("Heart")
                    .date(LocalDateTime.now().minusDays(3))
                    .hospitalName("Dhaka Medical College Hospital")
                    .doctorName("Dr. Rahim Chowdhury")
                    .imageUrl("https://images.unsplash.com/photo-1559757175-5700dde675bc?q=80&w=600&auto=format&fit=crop")
                    .findings("Left ventricular hypertrophy. Ejection fraction: 55%. Normal valvular structure.")
                    .impression("Mild Left Ventricular Hypertrophy, consistent with chronic hypertension.")
                    .build());

            bloodDonorRepository.save(BloodDonor.builder()
                    .patient(patientJudge)
                    .bloodGroup("O+")
                    .lastDonationDate(LocalDate.now().minusMonths(4))
                    .active(true)
                    .build());

            bloodRequestRepository.save(BloodRequest.builder()
                    .patientName("Judge Blood Case")
                    .bloodGroup("O+")
                    .urgency("High")
                    .hospital("Dhaka Medical College Hospital")
                    .location("Dhanmondi, Dhaka")
                    .timeline("Within 3 hours")
                    .status("Pending")
                    .previousDiseaseHistory("No major chronic diseases")
                    .build());
        }
    }
}
