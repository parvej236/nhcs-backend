package com.amrapari.nhcsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicDoctorDto {
    private Long id;
    private String name;
    private String specialization;
    private String hospital;
    private Integer fee;
    private Integer experience;
    private Double rating;
    private Long queueCount;
}
