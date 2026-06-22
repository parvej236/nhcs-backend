package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
}
