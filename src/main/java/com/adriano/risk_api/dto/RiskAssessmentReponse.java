package com.adriano.risk_api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskAssessmentReponse {
    private Long id;
    private String clientName;
    private Integer riskScore;
}
