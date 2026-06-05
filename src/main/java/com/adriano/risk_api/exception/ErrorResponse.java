package com.adriano.risk_api.exception;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ErrorResponse {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;
}
