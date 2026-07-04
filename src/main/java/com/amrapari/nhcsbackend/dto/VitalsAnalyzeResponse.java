package com.amrapari.nhcsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VitalsAnalyzeResponse {
    private String category;
    private String severity; // success, warning, danger
    private String bpVal;
    private String glucoseVal;
    private String summary;
    private List<String> recommendations;
}
