package com.adriano.risk_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
class RiskAssessmentRequest {

    @NotBlank
    private String clientName;

    @NotNull
    private String riskScore;
}
