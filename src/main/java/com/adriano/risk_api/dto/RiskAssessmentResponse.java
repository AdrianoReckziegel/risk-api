package com.adriano.risk_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class RiskAssessmentResponse {

    private Long id;

    private Long customerId;

    private LocalDate assessmentDate;

    private Integer riskScore;

    private String riskLevel;

    private String decision;

    private Instant createdAt;

}
