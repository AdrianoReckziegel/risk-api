package com.adriano.risk_api.dto;

import com.adriano.risk_api.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RiskResult {

    private Integer score;

    private RiskLevel riskLevel;

}
