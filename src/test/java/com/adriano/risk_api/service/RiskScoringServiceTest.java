package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.RiskResult;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.entity.RiskAssessment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {

    private RiskScoringService service;

    @BeforeEach
    void setUp() {
        service = new RiskScoringService();
    }

    private Customer customer(int creditScore, double income) {
        Customer customer = new Customer();
        customer.setCreditScore(creditScore);
        customer.setAnnualIncome(BigDecimal.valueOf(income));
        return customer;
    }

    @Test
    void shouldReturnLowRiskForHighCreditAndHighIncome() {

        Customer customer = customer(800, 120000);

        RiskResult result = service.calculate(customer);

        assertEquals(100, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void shouldReturnHighRiskForLowCreditAndLowIncome() {

        Customer customer = customer(600, 30000);

        RiskResult result = service.calculate(customer);

        assertEquals(20, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    void shouldReturnMediumRiskForHighCreditAndLowIncome() {

        Customer customer = customer(800, 30000);

        RiskResult result = service.calculate(customer);

        assertEquals(60, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.MEDIUM, result.getRiskLevel());
    }

    @Test
    void shouldReturnLowRiskAtCreditBoundary750() {

        Customer customer = customer(750, 100000);

        RiskResult result = service.calculate(customer);

        assertEquals(100, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void shouldReturnHighRiskAtCreditBoundary650() {

        Customer customer = customer(650, 30000);

        RiskResult result = service.calculate(customer);

        assertEquals(40, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.HIGH, result.getRiskLevel());
    }

    @Test
    void shouldReturnLowRiskAtIncomeBoundary100000() {

        Customer customer = customer(800, 100000);

        RiskResult result = service.calculate(customer);

        assertEquals(100, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.LOW, result.getRiskLevel());
    }

    @Test
    void shouldReturnMediumRiskAtIncomeBoundary50000() {

        Customer customer = customer(600, 50000);

        RiskResult result = service.calculate(customer);

        assertEquals(40, result.getScore());
        assertEquals(RiskAssessment.RiskLevel.HIGH, result.getRiskLevel());
    }
}