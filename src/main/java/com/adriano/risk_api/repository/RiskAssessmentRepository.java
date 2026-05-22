package com.adriano.risk_api.repository;

import com.adriano.risk_api.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
It automatically provides (from JpaRepository):

save()
findById()
findAll()
deleteById()
*/

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long>{
    List<RiskAssessment> findByCustomerId(Long customerId);
}
