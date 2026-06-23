package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.CreateRiskAssessmentRequest;
import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-assessments")
public class RiskAssessmentController {
    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping("/calculate/{customerId}")
    public ResponseEntity<RiskAssessmentResponse> calculate(@PathVariable Long customerId) {
        return ResponseEntity.ok(riskAssessmentService.calculateAssessment(customerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(riskAssessmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RiskAssessmentResponse>> getAll() {
        return ResponseEntity.ok(riskAssessmentService.getAll());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RiskAssessmentResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(riskAssessmentService.getByCustomerId(customerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> update(
            @PathVariable Long id,
            @RequestBody CreateRiskAssessmentRequest request) {

        return ResponseEntity.ok(riskAssessmentService.updateAssessment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        riskAssessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }
}
