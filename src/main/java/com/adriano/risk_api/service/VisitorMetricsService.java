package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.VisitorEventRequest;
import com.adriano.risk_api.dto.VisitorMetricsSummaryResponse;
import com.adriano.risk_api.entity.VisitorEvent;
import com.adriano.risk_api.repository.VisitorEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorMetricsService {

    private final VisitorEventRepository visitorEventRepository;
    private final EmailService emailService;

    @Transactional
    public void recordVisit(VisitorEventRequest request, HttpServletRequest httpRequest) {
        try {
            String clientIp = resolveClientIp(httpRequest);
            String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : "";
            if (userAgent == null) userAgent = "";

            String browser = detectBrowser(userAgent);
            String os = detectOperatingSystem(userAgent);
            String deviceType = detectDeviceType(userAgent);

            String rawReferrer = request != null && request.getReferrer() != null ? request.getReferrer().trim() : "direct";
            String referrerSource = resolveReferrerSource(rawReferrer);

            String timezone = request != null && request.getTimezone() != null ? request.getTimezone().trim() : "";
            String language = request != null && request.getLanguage() != null ? request.getLanguage().trim() : "";
            String location = resolveLocation(timezone, language, clientIp);

            String eventType = (request != null && request.getEventType() != null && !request.getEventType().isBlank())
                    ? request.getEventType().trim().toUpperCase()
                    : "PAGE_VIEW";

            String path = request != null && request.getPath() != null ? request.getPath().trim() : "/";
            String sessionId = request != null && request.getSessionId() != null ? request.getSessionId().trim() : "";
            String screenRes = request != null && request.getScreenResolution() != null ? request.getScreenResolution().trim() : "";

            VisitorEvent event = VisitorEvent.builder()
                    .eventType(eventType)
                    .clientIp(clientIp)
                    .userAgent(truncate(userAgent, 500))
                    .browser(browser)
                    .operatingSystem(os)
                    .deviceType(deviceType)
                    .referrer(truncate(rawReferrer, 500))
                    .referrerSource(referrerSource)
                    .location(location)
                    .timezone(timezone)
                    .language(language)
                    .screenResolution(screenRes)
                    .path(path)
                    .sessionId(sessionId)
                    .timestamp(Instant.now())
                    .build();

            visitorEventRepository.save(event);

            // Send notification on View-Only Demo access (often used by prospective recruiters)
            if ("VIEW_ONLY_ACCESS".equalsIgnoreCase(eventType)) {
                emailService.sendVisitorAlert(
                        eventType,
                        referrerSource,
                        location,
                        String.format("%s on %s (%s)", browser, os, deviceType),
                        clientIp
                );
            }
        } catch (Exception ex) {
            log.error("Failed to record visitor telemetry: {}", ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public VisitorMetricsSummaryResponse getMetricsSummary() {
        long totalVisits = visitorEventRepository.count();
        long uniqueVisitors = visitorEventRepository.countDistinctSessions();
        if (uniqueVisitors == 0 && totalVisits > 0) {
            uniqueVisitors = 1;
        }

        long viewOnlyVisits = visitorEventRepository.countByEventType("VIEW_ONLY_ACCESS");

        List<VisitorEvent> recent = visitorEventRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 30));

        Map<String, Long> topReferrers = recent.stream()
                .filter(v -> v.getReferrerSource() != null && !v.getReferrerSource().isBlank())
                .collect(Collectors.groupingBy(VisitorEvent::getReferrerSource, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Map<String, Long> topLocations = recent.stream()
                .filter(v -> v.getLocation() != null && !v.getLocation().isBlank())
                .collect(Collectors.groupingBy(VisitorEvent::getLocation, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        Map<String, Long> topDevices = recent.stream()
                .filter(v -> v.getDeviceType() != null && !v.getDeviceType().isBlank())
                .collect(Collectors.groupingBy(VisitorEvent::getDeviceType, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<VisitorMetricsSummaryResponse.VisitorEventItem> items = recent.stream()
                .map(v -> VisitorMetricsSummaryResponse.VisitorEventItem.builder()
                        .id(v.getId())
                        .eventType(v.getEventType())
                        .referrerSource(v.getReferrerSource())
                        .location(v.getLocation())
                        .device(v.getDeviceType())
                        .browser(v.getBrowser())
                        .operatingSystem(v.getOperatingSystem())
                        .path(v.getPath())
                        .timestamp(v.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return VisitorMetricsSummaryResponse.builder()
                .totalVisits(totalVisits)
                .uniqueVisitors(uniqueVisitors)
                .viewOnlyVisits(viewOnlyVisits)
                .topReferrers(topReferrers)
                .topLocations(topLocations)
                .topDevices(topDevices)
                .recentVisits(items)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "Unknown";
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP" };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "Unknown";
    }

    private String detectBrowser(String userAgent) {
        String lower = userAgent.toLowerCase();
        if (lower.contains("edg/")) return "Microsoft Edge";
        if (lower.contains("chrome/") && !lower.contains("edg/")) return "Google Chrome";
        if (lower.contains("safari/") && !lower.contains("chrome/")) return "Apple Safari";
        if (lower.contains("firefox/")) return "Mozilla Firefox";
        if (lower.contains("opera/") || lower.contains("opr/")) return "Opera";
        return "Standard Browser";
    }

    private String detectOperatingSystem(String userAgent) {
        String lower = userAgent.toLowerCase();
        if (lower.contains("iphone") || lower.contains("ipad")) return "iOS";
        if (lower.contains("macintosh") || lower.contains("mac os x")) return "macOS";
        if (lower.contains("android")) return "Android";
        if (lower.contains("windows")) return "Windows";
        if (lower.contains("linux")) return "Linux";
        return "Other OS";
    }

    private String detectDeviceType(String userAgent) {
        String lower = userAgent.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet")) return "Tablet";
        if (lower.contains("mobile") || lower.contains("iphone") || lower.contains("android")) return "Mobile";
        return "Desktop";
    }

    private String resolveReferrerSource(String referrer) {
        if (referrer == null || referrer.isBlank() || "direct".equalsIgnoreCase(referrer)) {
            return "Direct Navigation";
        }
        String lower = referrer.toLowerCase();
        if (lower.contains("linkedin.com")) return "LinkedIn";
        if (lower.contains("github.com")) return "GitHub";
        if (lower.contains("indeed.com")) return "Indeed";
        if (lower.contains("glassdoor.com")) return "Glassdoor";
        if (lower.contains("google.com")) return "Google Search";
        if (lower.contains("adreck.ca") || lower.contains("adreck.com")) return "Portfolio (adreck)";

        try {
            URI uri = URI.create(referrer);
            if (uri.getHost() != null) {
                return uri.getHost().replaceFirst("^www\\.", "");
            }
        } catch (Exception ignored) {
        }
        return "External Link";
    }

    private String resolveLocation(String timezone, String language, String clientIp) {
        if (timezone != null && !timezone.isBlank()) {
            switch (timezone) {
                case "America/Toronto":
                    return "Toronto, ON (Canada)";
                case "America/Montreal":
                    return "Montreal, QC (Canada)";
                case "America/Vancouver":
                    return "Vancouver, BC (Canada)";
                case "America/Edmonton":
                    return "Edmonton / Calgary (Canada)";
                case "America/Halifax":
                    return "Halifax, NS (Canada)";
                case "America/New_York":
                    return "New York / Eastern (US)";
                case "America/Chicago":
                    return "Chicago / Central (US)";
                case "America/Los_Angeles":
                    return "San Francisco / LA (US)";
                case "America/Denver":
                    return "Denver / Mountain (US)";
                case "America/Sao_Paulo":
                    return "Sao Paulo (Brazil)";
                case "Europe/London":
                    return "London (United Kingdom)";
                case "Europe/Paris":
                    return "Paris (France)";
                case "Europe/Berlin":
                    return "Berlin (Germany)";
                case "Europe/Dublin":
                    return "Dublin (Ireland)";
                case "Asia/Tokyo":
                    return "Tokyo (Japan)";
                default:
                    if (timezone.contains("/")) {
                        String[] parts = timezone.split("/");
                        return parts[parts.length - 1].replace("_", " ") + " (" + parts[0] + ")";
                    }
                    return timezone;
            }
        }
        if (clientIp != null && (clientIp.startsWith("127.") || clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.equals("::1"))) {
            return "Localhost / Internal";
        }
        return "Online Visitor";
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

}
