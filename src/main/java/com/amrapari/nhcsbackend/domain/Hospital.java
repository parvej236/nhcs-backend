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

    // Explicit Getters and Setters to bypass IDE LSP analyzer warnings
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public Integer getTotalBeds() { return totalBeds != null ? totalBeds : 0; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }

    public Integer getOccupiedBeds() { return occupiedBeds != null ? occupiedBeds : 0; }
    public void setOccupiedBeds(Integer occupiedBeds) { this.occupiedBeds = occupiedBeds; }

    public Integer getComplianceScore() { return complianceScore != null ? complianceScore : 0; }
    public void setComplianceScore(Integer complianceScore) { this.complianceScore = complianceScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
