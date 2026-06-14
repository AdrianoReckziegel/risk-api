package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.entity.RiskAssessment;
import java.util.List;

public interface RiskAssessmentService {

    RiskAssessmentResponse createAssessment(RiskAssessment riskAssessment);

    RiskAssessmentResponse getById(Long id);

    List<RiskAssessmentResponse> getAll();

    List<RiskAssessmentResponse> getByCustomerId(Long customerId);

    RiskAssessmentResponse updateAssessment(Long id, RiskAssessment updatedAssessment);

    void deleteAssessment(Long id);

    RiskAssessmentResponse calculateAssessment(Long customerId);

}
