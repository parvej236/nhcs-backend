package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {
    @Id
    private String id; // format APP-XXXX

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private LocalDate date;
    private String timeSlot;
    private String queueNumber;
    private String status;
    private String hospitalName;

    // Slice 5 fields
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private String arrivalStatus; // AWAITING, CHECKED_IN, COMPLETED
    private String visitType; // First Consultation, Follow-up, Emergency, Referral
    private String riskIndicator; // Low, Moderate, High, Emergency

    // Explicit Getters and Setters to bypass IDE LSP analyzer warnings
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getQueueNumber() { return queueNumber; }
    public void setQueueNumber(String queueNumber) { this.queueNumber = queueNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getArrivalStatus() { return arrivalStatus; }
    public void setArrivalStatus(String arrivalStatus) { this.arrivalStatus = arrivalStatus; }

    public String getVisitType() { return visitType; }
    public void setVisitType(String visitType) { this.visitType = visitType; }

    public String getRiskIndicator() { return riskIndicator; }
    public void setRiskIndicator(String riskIndicator) { this.riskIndicator = riskIndicator; }

    // Serialization helper getters for frontend mapping
    public String getPatientName() {
        return patient != null ? patient.getFullName() : null;
    }

    public Integer getPatientAge() {
        if (patient == null || patient.getDateOfBirth() == null) return null;
        return java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears();
    }

    public String getPatientGender() {
        return patient != null ? patient.getGender() : null;
    }

    public String getPatientHealthId() {
        // Unified Health ID used across the whole system. Must match the format
        // emitted by PatientController.mapToProfileDto ("NUD-000-<id>") and the
        // resolver logic (trailing numeric segment = patient id) so that the
        // doctor's Clinical Workspace and the treatment-submission endpoints all
        // resolve back to the SAME real patient.
        return patient != null ? "NUD-000-" + patient.getId() : null;
    }

    public String getBpSystolic() {
        return patient != null ? patient.getBpSystolic() : null;
    }

    public String getBpDiastolic() {
        return patient != null ? patient.getBpDiastolic() : null;
    }

    public String getBloodGlucose() {
        return patient != null ? patient.getBloodGlucose() : null;
    }

    public String getPatientBloodGroup() {
        return patient != null ? patient.getBloodGroup() : null;
    }
}
