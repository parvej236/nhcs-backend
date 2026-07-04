package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "blood_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;
    private String bloodGroup;
    private String urgency;
    private String hospital;
    private String location;
    private String timeline;
    private String status; // "Pending", "Accepted", "Declined"
    private String previousDiseaseHistory;

    @ManyToOne
    @JoinColumn(name = "accepted_by_patient_id", referencedColumnName = "id")
    private Patient acceptedBy;

    // Explicit Getters and Setters to avoid any IDE plugin/LSP issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPreviousDiseaseHistory() { return previousDiseaseHistory; }
    public void setPreviousDiseaseHistory(String previousDiseaseHistory) { this.previousDiseaseHistory = previousDiseaseHistory; }
    public Patient getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(Patient acceptedBy) { this.acceptedBy = acceptedBy; }
}
