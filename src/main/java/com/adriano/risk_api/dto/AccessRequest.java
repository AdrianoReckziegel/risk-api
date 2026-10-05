package com.adriano.risk_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    private String email;

    private String name;

    @NotBlank(message = "Reason is required")
    private String reason;

}
