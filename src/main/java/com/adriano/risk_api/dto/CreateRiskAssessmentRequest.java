package com.adriano.risk_api.dto;

import jakarta.validation.constraints.NotNull;

public class CreateRiskAssessmentRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    
}
