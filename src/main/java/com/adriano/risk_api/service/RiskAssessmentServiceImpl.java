package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.exception.RiskAssessmentNotFoundException;
import com.adriano.risk_api.repository.RiskAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RiskAssessmentServiceImpl implements RiskAssessmentService{
    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessmentServiceImpl(RiskAssessmentRepository riskAssessmentRepository) {
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    @Override
    public RiskAssessmentResponse createAssessment(RiskAssessment riskAssessment) {

        RiskAssessment saved = riskAssessmentRepository.save(riskAssessment);

        return RiskAssessmentResponse.builder()
                .id(saved.getId())
                .customerId(saved.getCustomer().getId())
                .riskScore(saved.getRiskScore())
                .riskLevel(saved.getRiskLevel().name())
                .createdAt(saved.getCreatedAt())
                .build();

    }

    @Override
    public RiskAssessmentResponse getById(Long id) {

        RiskAssessment assessment = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(
                        "Risk assessment not found with id: " + id
                ));

        return RiskAssessmentResponse.builder()
                .id(assessment.getId())
                .customerId(assessment.getCustomer().getId())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel().name())
                .createdAt(assessment.getCreatedAt())
                .build();
    }

    @Override
    public List<RiskAssessmentResponse> getAll() {

        return riskAssessmentRepository.findAll().stream()
                .map(a -> RiskAssessmentResponse.builder()
                        .id(a.getId())
                        .customerId(a.getCustomer().getId())
                        .riskScore(a.getRiskScore())
                        .riskLevel(a.getRiskLevel().name())
                        .createdAt(a.getCreatedAt())
                        .build()
                )
                .toList();
    }

    @Override
    public List<RiskAssessmentResponse> getByCustomerId(Long customerId) {

        return riskAssessmentRepository.findByCustomerId(customerId).stream()
                .map(a -> RiskAssessmentResponse.builder()
                        .id(a.getId())
                        .customerId(a.getCustomer().getId())
                        .riskScore(a.getRiskScore())
                        .riskLevel(a.getRiskLevel().name())
                        .createdAt(a.getCreatedAt())
                        .build()
                )
                .toList();
    }

    @Override
    public RiskAssessmentResponse updateAssessment(Long id, RiskAssessment updatedAssessment) {

        RiskAssessment existing = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(
                        "RiskAssessment not found with id " + id));

        existing.setRiskScore(updatedAssessment.getRiskScore());
        existing.setDecision(updatedAssessment.getDecision());
        existing.setAssessmentDate(updatedAssessment.getAssessmentDate());

        RiskAssessment saved = riskAssessmentRepository.save(existing);

        return RiskAssessmentResponse.builder()
                .id(saved.getId())
                .customerId(saved.getCustomer().getId())
                .riskScore(saved.getRiskScore())
                .riskLevel(saved.getRiskLevel().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public void deleteAssessment(Long id) {
        riskAssessmentRepository.deleteById(id);
    }
}
