package com.adriano.risk_api.service;

import com.adriano.risk_api.entity.RiskAssessment;
import java.util.List;
import java.util.Optional;

public interface RiskAssessmentService {

    RiskAssessment createAssessment(RiskAssessment riskAssessment);

    Optional<RiskAssessment> getById(Long id);

    List<RiskAssessment> getAll();

    List<RiskAssessment> getByCustomerId(Long customerId);

    RiskAssessment updateAssessment(Long id, RiskAssessment updatedAssessment);

    void deleteAssessment(Long id);

}
