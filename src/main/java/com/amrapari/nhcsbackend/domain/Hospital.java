package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hospitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String facilityId;
    
    private String name;
    private String division;
    private String classification;
    private Integer totalBeds;
    private Integer occupiedBeds;
    private Integer complianceScore;
    private String status;
}
