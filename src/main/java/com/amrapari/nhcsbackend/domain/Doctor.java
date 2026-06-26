package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String fullName;
    private String specialization;
    private String licenseNumber;
    private String contactNumber;
    private String hospitalAffiliation;

    private Double rating;
    private Integer experienceYears;
    private Integer consultationFee;
    private String imageUrl;
}

