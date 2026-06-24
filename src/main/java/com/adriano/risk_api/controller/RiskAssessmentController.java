package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.CreateRiskAssessmentRequest;
import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.service.RiskAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Risk Assessments",
        description = "Risk assessment operations")
@RestController
@RequestMapping("/api/risk-assessments")
public class RiskAssessmentController {
    private final RiskAssessmentService riskAssessmentService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @Operation(
            summary = "Calculate risk assessment",
            description = "Calculates and stores a risk assessment for a customer"
    )
    @PostMapping("/calculate/{customerId}")
    public ResponseEntity<RiskAssessmentResponse> calculate(@PathVariable Long customerId) {
        return ResponseEntity.ok(riskAssessmentService.calculateAssessment(customerId));
    }

    @Operation(
            summary = "Get risk assessment by ID",
            description = "Retrieves a risk assessment by its ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(riskAssessmentService.getById(id));
    }

    @Operation(
            summary = "Get all risk assessments",
            description = "Retrieves all risk assessments"
    )
    @GetMapping
    public ResponseEntity<List<RiskAssessmentResponse>> getAll() {
        return ResponseEntity.ok(riskAssessmentService.getAll());
    }

    @Operation(
            summary = "Get risk assessments by customer ID",
            description = "Retrieves all risk assessments for a specific customer"
    )
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RiskAssessmentResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(riskAssessmentService.getByCustomerId(customerId));
    }

    @Operation(
            summary = "Update risk assessment",
            description = "Updates an existing risk assessment by its ID"
    )
    @PutMapping("/{id}")
    public ResponseEntity<RiskAssessmentResponse> update(
            @PathVariable Long id,
            @RequestBody CreateRiskAssessmentRequest request) {

        return ResponseEntity.ok(riskAssessmentService.updateAssessment(id, request));
    }

    @Operation(
            summary = "Delete risk assessment",
            description = "Deletes a risk assessment by its ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        riskAssessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }
}
