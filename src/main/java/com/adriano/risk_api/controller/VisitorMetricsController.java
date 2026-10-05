package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.VisitorEventRequest;
import com.adriano.risk_api.dto.VisitorMetricsSummaryResponse;
import com.adriano.risk_api.service.VisitorMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Visitor Metrics", description = "Visitor telemetry and analytics endpoints")
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class VisitorMetricsController {

    private final VisitorMetricsService visitorMetricsService;

    @Operation(summary = "Track visitor event", description = "Public endpoint for recording page visits and view-only demo access")
    @PostMapping("/track")
    public ResponseEntity<Map<String, String>> track(
            @RequestBody(required = false) VisitorEventRequest request,
            HttpServletRequest httpRequest) {
        visitorMetricsService.recordVisit(request, httpRequest);
        return ResponseEntity.ok(Map.of("status", "recorded"));
    }

    @Operation(summary = "Get visitor metrics summary", description = "Returns aggregated visitor analytics, referrers, locations, and live visit feed")
    @GetMapping("/summary")
    public ResponseEntity<VisitorMetricsSummaryResponse> getSummary() {
        return ResponseEntity.ok(visitorMetricsService.getMetricsSummary());
    }

}
