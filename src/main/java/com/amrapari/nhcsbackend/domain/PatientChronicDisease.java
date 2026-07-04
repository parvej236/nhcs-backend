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

    // Explicit Getters and Setters to bypass IDE LSP analyzer warnings
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDiagnosedDate() { return diagnosedDate; }
    public void setDiagnosedDate(LocalDate diagnosedDate) { this.diagnosedDate = diagnosedDate; }
}
