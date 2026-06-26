package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "lab_test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_report_id", nullable = false)
    @JsonBackReference
    private LabReport labReport;

    private String parameter;
    private String value;
    private String unit;
    private String referenceRange;
    private String status;
}
