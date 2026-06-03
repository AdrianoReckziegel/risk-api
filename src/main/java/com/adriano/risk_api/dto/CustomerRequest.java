package com.adriano.risk_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {

    @NotBlank
    private String externalId;

    @NotBlank
    private String name;

    private LocalDate birthDate;

    private String email;

}
