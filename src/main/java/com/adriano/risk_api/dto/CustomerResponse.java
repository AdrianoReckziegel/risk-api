package com.adriano.risk_api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class CustomerResponse {

    private Long id;

    private String externalId;

    private String name;

    private LocalDate birthDate;

    private String email;

    private Instant createdAt;

}
