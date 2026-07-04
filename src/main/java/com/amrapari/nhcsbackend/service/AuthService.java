package com.amrapari.nhcsbackend.service;

import com.amrapari.nhcsbackend.domain.Patient;
import com.amrapari.nhcsbackend.domain.Role;
import com.amrapari.nhcsbackend.domain.User;
import com.amrapari.nhcsbackend.dto.AuthRequest;
import com.amrapari.nhcsbackend.dto.AuthResponse;
import com.amrapari.nhcsbackend.dto.RegisterRequest;
import com.amrapari.nhcsbackend.repository.PatientRepository;
import com.amrapari.nhcsbackend.repository.UserRepository;
import com.amrapari.nhcsbackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthResponse register(RegisterRequest request) {
        // Public self-registration is PATIENT-only (Locked Decision). Any elevated
        // role sent in the request is ignored — Doctor / Hospital / Admin accounts
        // are seeded, never self-created. See DataInitializer.
        Set<Role> roles = new HashSet<>();
        roles.add(Role.PATIENT);

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();
        userRepository.save(user);

        var patient = Patient.builder()
                .user(user)
                .fullName(request.getFullName())
                .build();
        patientRepository.save(patient);

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
