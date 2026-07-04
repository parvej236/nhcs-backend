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
}
