package com.adriano.risk_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class CustomerRequest {

    @NotBlank
    private String externalId;

    @NotBlank
    private String name;

    private LocalDate birthDate;

    private String email;

    // Manually provided fields
    private Integer creditScore;

    private BigDecimal annualIncome;

}
