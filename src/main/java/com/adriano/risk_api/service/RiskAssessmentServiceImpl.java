package com.adriano.risk_api.service;

import com.adriano.risk_api.entity.RiskAssessment;
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
    public RiskAssessment createAssessment(RiskAssessment riskAssessment) {
        return riskAssessmentRepository.save(riskAssessment);
    }

    @Override
    public Optional<RiskAssessment> getById(Long id) {
        return riskAssessmentRepository.findById(id);
    }

    @Override
    public List<RiskAssessment> getAll() {
        return riskAssessmentRepository.findAll();
    }

    @Override
    public List<RiskAssessment> getByCustomerId(Long customerId) {
        return riskAssessmentRepository.findByCustomerId(customerId);
    }

    @Override
    public RiskAssessment updateAssessment(Long id, RiskAssessment updatedAssessment) {
        return riskAssessmentRepository.findById(id)
                .map(existing -> {
                    existing.setRiskScore(updatedAssessment.getRiskScore());
                    existing.setDecision(updatedAssessment.getDecision());
                    existing.setAssessmentDate(updatedAssessment.getAssessmentDate());

                    return riskAssessmentRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("RiskAssessment not found with id " + id));
    }

    @Override
    public void deleteAssessment(Long id) {
        riskAssessmentRepository.deleteById(id);
    }
}
