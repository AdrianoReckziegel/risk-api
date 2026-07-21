package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.RiskResult;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.entity.RiskLevel;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    public RiskResult calculate(Customer customer) {

        int score = 0;

        if (customer.getCreditScore() >= 750) {
            score += 60;
        } else if (customer.getCreditScore() >= 650) {
            score += 40;
        } else {
            score += 20;
        }

        if (customer.getAnnualIncome().doubleValue() >= 100000) {
            score += 40;
        } else if (customer.getAnnualIncome().doubleValue() >= 50000) {
            score += 20;
        }

        RiskLevel level;

        if (score >= 80) {
            level = RiskLevel.LOW;
        } else if (score >= 50) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.HIGH;
        }

        return new RiskResult(score, level);
    }
}
