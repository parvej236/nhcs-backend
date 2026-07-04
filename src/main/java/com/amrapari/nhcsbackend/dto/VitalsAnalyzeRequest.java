package com.amrapari.nhcsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VitalsAnalyzeRequest {
    private String symptomsText;
    private Integer bpSystolic;
    private Integer bpDiastolic;
    private Double glucose;
}
