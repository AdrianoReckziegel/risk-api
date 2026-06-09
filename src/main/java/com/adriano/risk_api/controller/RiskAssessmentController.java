package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-assessments")
@RequiredArgsConstructor

public class RiskAssessmentController {

    private final RiskAssessmentService service;

    @PostMapping
    public RiskAssessmentResponse create(@RequestBody RiskAssessment risk) {
        return service.createAssessment(risk);
    }

    @GetMapping("/{id}")
    public RiskAssessmentResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
