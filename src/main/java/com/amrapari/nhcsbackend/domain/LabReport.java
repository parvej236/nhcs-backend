package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lab_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabReport {
    @Id
    private String id; // format LR-XXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String testName;
    private String category;
    private LocalDateTime date;
    private String hospitalName;
    private String doctorName;
    private String status;

    @Lob
    @Column(length = 2000)
    private String aiInterpretation;

    @OneToMany(mappedBy = "labReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<LabTestResult> results = new ArrayList<>();
}
