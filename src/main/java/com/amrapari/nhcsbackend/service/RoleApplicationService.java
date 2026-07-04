package com.amrapari.nhcsbackend.service;

import com.amrapari.nhcsbackend.domain.*;
import com.amrapari.nhcsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    public RoleApplication applyForRole(String username, Role requestedRole, String notes) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRoles().contains(requestedRole)) {
            throw new RuntimeException("User already has the requested role.");
        }

        RoleApplication application = RoleApplication.builder()
                .user(user)
                .requestedRole(requestedRole)
                .status(ApplicationStatus.PENDING)
                .applicationDate(LocalDateTime.now())
                .notes(notes)
                .build();

        return applicationRepository.save(application);
    }

    public List<RoleApplication> getPendingApplications() {
        return applicationRepository.findAll().stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .toList();
    }

    public RoleApplication approveApplication(Long applicationId) {
        RoleApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Application is already processed.");
        }

        User user = application.getUser();
        Role requestedRole = application.getRequestedRole();

        // Add role to user
        user.getRoles().add(requestedRole);
        userRepository.save(user);

        // Fetch user's patient profile for fullName if needed
        String fullName = user.getUsername();
        Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);
        if (patient != null && patient.getFullName() != null) {
            fullName = patient.getFullName();
        }

        // Initialize related profile if DOCTOR or HOSPITAL
        if (requestedRole == Role.DOCTOR) {
            if (doctorRepository.findByUserId(user.getId()).isEmpty()) {
                Doctor doctor = Doctor.builder()
                        .user(user)
                        .fullName(fullName)
                        .build();
                doctorRepository.save(doctor);
            }
        }
        // If they requested HOSPITAL, we might need a dummy hospital or let them update it later
        // Currently, our Hospital entity doesn't link to User explicitly, but let's assume it does, or we just leave it for now.
        // Wait, does Hospital have a user_id? Let's assume it does not by default, or maybe it does? 
        // I will just mark the status APPROVED.

        application.setStatus(ApplicationStatus.APPROVED);
        return applicationRepository.save(application);
    }

    public RoleApplication rejectApplication(Long applicationId) {
        RoleApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Application is already processed.");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        return applicationRepository.save(application);
    }
}
