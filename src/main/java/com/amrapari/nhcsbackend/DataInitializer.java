package com.amrapari.nhcsbackend;

import com.amrapari.nhcsbackend.domain.Role;
import com.amrapari.nhcsbackend.domain.User;
import com.amrapari.nhcsbackend.domain.Hospital;
import com.amrapari.nhcsbackend.repository.UserRepository;
import com.amrapari.nhcsbackend.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createMockUserIfNotExists("patient", "patient@nhcs.gov", Role.PATIENT);
        createMockUserIfNotExists("doctor", "doctor@nhcs.gov", Role.DOCTOR);
        createMockUserIfNotExists("hospital", "hospital@nhcs.gov", Role.HOSPITAL);
        createMockUserIfNotExists("govt", "govt@nhcs.gov", Role.GOVT);
        
        createMockHospitalsIfEmpty();
    }

    private void createMockUserIfNotExists(String username, String email, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode("password123"))
                    .role(role)
                    .build();
            userRepository.save(user);
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
}
