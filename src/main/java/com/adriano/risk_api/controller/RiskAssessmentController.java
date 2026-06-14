package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-assessments")
public class RiskAssessmentController {
    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping("/calculate/{customerId}")
    public ResponseEntity<RiskAssessmentResponse> calculate(@PathVariable Long customerId) {

        RiskAssessmentResponse response =
                riskAssessmentService.calculateAssessment(customerId);

        return ResponseEntity.ok(response);
    }
}
