package com.adriano.risk_api.repository;

import com.adriano.risk_api.entity.Risk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskRepository extends JpaRepository<Risk, Long>{
}
