package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "patient_allergies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAllergy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonBackReference
    private Patient patient;

    private String allergen;
    private String severity;
    private String reaction;

    // Explicit Getters and Setters to bypass IDE LSP analyzer warnings
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getAllergen() { return allergen; }
    public void setAllergen(String allergen) { this.allergen = allergen; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
}
