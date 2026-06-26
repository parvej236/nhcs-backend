package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;

@Entity
@Table(name = "patient_chronic_diseases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientChronicDisease {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;

    private String diseaseName;
    private String status;
    private LocalDate diagnosedDate;
}
