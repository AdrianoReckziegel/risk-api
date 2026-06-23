package com.adriano.risk_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@Builder
public class CustomerResponse {

    private Long id;

    private String externalId;

    private String name;

    private LocalDate birthDate;

    private String email;

    private Instant createdAt;

    private Integer creditScore;

    private BigDecimal annualIncome;

}
