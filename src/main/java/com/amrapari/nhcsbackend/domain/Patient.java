package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String contactNumber;
    private String address;
    private String occupation;
    private String maritalStatus;
    private String presentAddress;
    private String permanentAddress;
    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;

    private String nationalId;
    private String bpSystolic;
    private String bpDiastolic;
    private String bloodGlucose;
    private String heartRate;
    private String weight;
    private LocalDateTime vitalsLastUpdated;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<PatientAllergy> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<PatientChronicDisease> chronicDiseases = new ArrayList<>();

    // Explicit Getters and Setters to bypass IDE LSP analyzer warnings
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }

    public String getPresentAddress() { return presentAddress; }
    public void setPresentAddress(String presentAddress) { this.presentAddress = presentAddress; }

    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(String emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getBpSystolic() { return bpSystolic; }
    public void setBpSystolic(String bpSystolic) { this.bpSystolic = bpSystolic; }

    public String getBpDiastolic() { return bpDiastolic; }
    public void setBpDiastolic(String bpDiastolic) { this.bpDiastolic = bpDiastolic; }

    public String getBloodGlucose() { return bloodGlucose; }
    public void setBloodGlucose(String bloodGlucose) { this.bloodGlucose = bloodGlucose; }

    public String getHeartRate() { return heartRate; }
    public void setHeartRate(String heartRate) { this.heartRate = heartRate; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public LocalDateTime getVitalsLastUpdated() { return vitalsLastUpdated; }
    public void setVitalsLastUpdated(LocalDateTime vitalsLastUpdated) { this.vitalsLastUpdated = vitalsLastUpdated; }

    public List<PatientAllergy> getAllergies() { return allergies; }
    public void setAllergies(List<PatientAllergy> allergies) { this.allergies = allergies; }

    public List<PatientChronicDisease> getChronicDiseases() { return chronicDiseases; }
    public void setChronicDiseases(List<PatientChronicDisease> chronicDiseases) { this.chronicDiseases = chronicDiseases; }
}
