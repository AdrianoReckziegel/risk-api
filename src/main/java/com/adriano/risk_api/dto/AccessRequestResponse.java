package com.adriano.risk_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestResponse {

    private String message;

    private String recipient;

    private Instant timestamp;

}
