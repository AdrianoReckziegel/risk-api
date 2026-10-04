package com.adriano.risk_api.dto;

import com.adriano.risk_api.entity.Decision;
import com.adriano.risk_api.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RiskResult {

    private Integer score;

    private RiskLevel riskLevel;

    private Decision decision;

    public RiskResult(Integer score, RiskLevel riskLevel) {
        this.score = score;
        this.riskLevel = riskLevel;
    }

}
