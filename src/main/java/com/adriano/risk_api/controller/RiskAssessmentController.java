package com.adriano.risk_api.controller;

import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor

public class RiskAssessmentController {

    private final RiskAssessmentService service;

    @PostMapping
    public RiskAssessment create(@RequestBody RiskAssessment risk) {
        return service.createAssessment(risk);
    }
}
