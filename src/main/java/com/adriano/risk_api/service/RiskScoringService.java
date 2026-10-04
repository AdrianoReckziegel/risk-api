package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.RiskResult;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.entity.Decision;
import com.adriano.risk_api.entity.RiskLevel;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

    public RiskResult calculate(Customer customer) {

        int score = 0;

        if (customer != null) {
            Integer creditScore = customer.getCreditScore();
            if (creditScore != null) {
                if (creditScore >= 750) {
                    score += 60;
                } else if (creditScore >= 650) {
                    score += 40;
                } else {
                    score += 20;
                }
            }

            if (customer.getAnnualIncome() != null) {
                double income = customer.getAnnualIncome().doubleValue();
                if (income >= 100000) {
                    score += 40;
                } else if (income >= 50000) {
                    score += 20;
                }
            }
        }

        RiskLevel level;
        Decision decision;

        if (score >= 80) {
            level = RiskLevel.LOW;
            decision = Decision.APPROVED;
        } else if (score >= 50) {
            level = RiskLevel.MEDIUM;
            decision = Decision.REVIEW;
        } else {
            level = RiskLevel.HIGH;
            decision = Decision.REJECTED;
        }

        return new RiskResult(score, level, decision);
    }
}
