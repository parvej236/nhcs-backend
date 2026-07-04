package com.amrapari.nhcsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicStatsDto {
    private long patients;
    private long doctors;
    private long hospitals;
}
