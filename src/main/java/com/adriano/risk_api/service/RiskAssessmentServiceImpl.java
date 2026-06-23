package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.CreateRiskAssessmentRequest;
import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.dto.RiskResult;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.exception.CustomerNotFoundException;
import com.adriano.risk_api.exception.RiskAssessmentNotFoundException;
import com.adriano.risk_api.repository.CustomerRepository;
import com.adriano.risk_api.repository.RiskAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RiskAssessmentServiceImpl implements RiskAssessmentService{

    private RiskAssessmentRepository riskAssessmentRepository;
    private final CustomerRepository customerRepository;
    private final RiskScoringService riskScoringService;

    public RiskAssessmentServiceImpl(
            RiskAssessmentRepository riskAssessmentRepository,
            CustomerRepository customerRepository,
            RiskScoringService riskScoringService) {

        this.riskAssessmentRepository = riskAssessmentRepository;
        this.customerRepository = customerRepository;
        this.riskScoringService = riskScoringService;
    }

    @Override
    public RiskAssessmentResponse createAssessment(CreateRiskAssessmentRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()
                ));

        RiskResult result = riskScoringService.calculate(customer);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setCustomer(customer);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setRiskScore(result.getScore());
        assessment.setRiskLevel(result.getRiskLevel());

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        return toResponse(saved);
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
    public RiskAssessmentResponse updateAssessment(Long id, CreateRiskAssessmentRequest request) {

        RiskAssessment existing = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(
                        "Risk assessment not found with id: " + id
                ));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()
                ));

        RiskResult result = riskScoringService.calculate(customer);

        existing.setCustomer(customer);
        existing.setRiskScore(result.getScore());
        existing.setRiskLevel(result.getRiskLevel());
        existing.setAssessmentDate(LocalDate.now());

        RiskAssessment saved = riskAssessmentRepository.save(existing);

        return toResponse(saved);
    }

    @Override
    public void deleteAssessment(Long id) {

        RiskAssessment assessment = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(
                        "Risk assessment not found with id: " + id
                ));

        riskAssessmentRepository.delete(assessment);
    }

    @Override
    public RiskAssessmentResponse calculateAssessment(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId
                ));

        RiskResult result = riskScoringService.calculate(customer);

        RiskAssessment assessment = new RiskAssessment();
        assessment.setCustomer(customer);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setRiskScore(result.getScore());
        assessment.setRiskLevel(result.getRiskLevel());

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        return RiskAssessmentResponse.builder()
                .id(saved.getId())
                .customerId(saved.getCustomer().getId())
                .riskScore(saved.getRiskScore())
                .riskLevel(saved.getRiskLevel().name())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private RiskAssessmentResponse toResponse(RiskAssessment a) {
        return RiskAssessmentResponse.builder()
                .id(a.getId())
                .customerId(a.getCustomer().getId())
                .riskScore(a.getRiskScore())
                .riskLevel(a.getRiskLevel().name())
                .createdAt(a.getCreatedAt())
                .build();
    }

}
