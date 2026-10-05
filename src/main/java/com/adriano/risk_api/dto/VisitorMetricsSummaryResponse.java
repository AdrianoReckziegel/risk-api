package com.adriano.risk_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorMetricsSummaryResponse {

    private long totalVisits;

    private long uniqueVisitors;

    private long viewOnlyVisits;

    private Map<String, Long> topReferrers;

    private Map<String, Long> topLocations;

    private Map<String, Long> topDevices;

    private List<VisitorEventItem> recentVisits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitorEventItem {
        private Long id;
        private String eventType;
        private String referrerSource;
        private String location;
        private String device;
        private String browser;
        private String operatingSystem;
        private String path;
        private Instant timestamp;
    }

}
