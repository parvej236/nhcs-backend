package com.amrapari.nhcsbackend.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "imaging_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImagingReport {
    @Id
    private String id; // format IM-XXXX

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String type;
    private String bodyPart;
    private LocalDateTime date;
    private String hospitalName;
    private String doctorName;
    private String imageUrl;

    @Lob
    @Column(length = 2000)
    private String findings;

    @Lob
    @Column(length = 2000)
    private String impression;
}
