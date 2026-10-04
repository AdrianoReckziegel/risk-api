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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;
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
        assessment.setDecision(result.getDecision());
        assessment.setInputSnapshot(buildInputSnapshot(customer));

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        return toResponse(saved);
    }

    @Override
    public RiskAssessmentResponse getById(Long id) {

        RiskAssessment assessment = riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(
                        "Risk assessment not found with id: " + id
                ));

        return toResponse(assessment);
    }

    @Override
    public List<RiskAssessmentResponse> getAll() {

        return riskAssessmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RiskAssessmentResponse> getByCustomerId(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(customerId));

        return riskAssessmentRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
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
        existing.setDecision(result.getDecision());
        existing.setInputSnapshot(buildInputSnapshot(customer));
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
        assessment.setDecision(result.getDecision());
        assessment.setInputSnapshot(buildInputSnapshot(customer));

        RiskAssessment saved = riskAssessmentRepository.save(assessment);

        return toResponse(saved);
    }

    private RiskAssessmentResponse toResponse(RiskAssessment a) {
        return RiskAssessmentResponse.builder()
                .id(a.getId())
                .customerId(a.getCustomer() != null ? a.getCustomer().getId() : null)
                .assessmentDate(a.getAssessmentDate())
                .riskScore(a.getRiskScore())
                .riskLevel(a.getRiskLevel() != null ? a.getRiskLevel().name() : null)
                .decision(a.getDecision() != null ? a.getDecision().name() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }

    private String buildInputSnapshot(Customer customer) {
        if (customer == null) {
            return null;
        }
        return String.format(
                "{\"externalId\":\"%s\",\"creditScore\":%s,\"annualIncome\":%s}",
                customer.getExternalId() != null ? customer.getExternalId() : "",
                customer.getCreditScore() != null ? customer.getCreditScore() : "null",
                customer.getAnnualIncome() != null ? customer.getAnnualIncome().toPlainString() : "null"
        );
    }

}
