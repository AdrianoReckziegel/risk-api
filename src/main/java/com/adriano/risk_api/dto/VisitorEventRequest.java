package com.adriano.risk_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorEventRequest {

    private String eventType;

    private String path;

    private String referrer;

    private String timezone;

    private String language;

    private String screenResolution;

    private String sessionId;

}
