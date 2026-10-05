package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.VisitorEventRequest;
import com.adriano.risk_api.dto.VisitorMetricsSummaryResponse;
import com.adriano.risk_api.entity.VisitorEvent;
import com.adriano.risk_api.repository.VisitorEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisitorMetricsServiceTest {

    @Mock
    private VisitorEventRepository visitorEventRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private HttpServletRequest httpRequest;

    private VisitorMetricsService visitorMetricsService;

    @BeforeEach
    void setUp() {
        visitorMetricsService = new VisitorMetricsService(visitorEventRepository, emailService);
    }

    @Test
    void shouldRecordVisitAndSendAlertOnViewOnlyAccess() {
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("198.51.100.24");

        VisitorEventRequest request = VisitorEventRequest.builder()
                .eventType("VIEW_ONLY_ACCESS")
                .path("/dashboard")
                .referrer("https://www.linkedin.com/feed/")
                .timezone("America/Toronto")
                .sessionId("test-session-123")
                .build();

        visitorMetricsService.recordVisit(request, httpRequest);

        ArgumentCaptor<VisitorEvent> captor = ArgumentCaptor.forClass(VisitorEvent.class);
        verify(visitorEventRepository).save(captor.capture());

        VisitorEvent saved = captor.getValue();
        assertEquals("VIEW_ONLY_ACCESS", saved.getEventType());
        assertEquals("LinkedIn", saved.getReferrerSource());
        assertEquals("Google Chrome", saved.getBrowser());
        assertEquals("macOS", saved.getOperatingSystem());
        assertEquals("Desktop", saved.getDeviceType());
        assertTrue(saved.getLocation().contains("Toronto"));
        assertEquals("198.51.100.24", saved.getClientIp());

        verify(emailService).sendVisitorAlert(
                eq("VIEW_ONLY_ACCESS"),
                eq("LinkedIn"),
                contains("Toronto"),
                contains("Chrome"),
                eq("198.51.100.24")
        );
    }

    @Test
    void shouldReturnMetricsSummary() {
        when(visitorEventRepository.count()).thenReturn(15L);
        when(visitorEventRepository.countDistinctSessions()).thenReturn(10L);
        when(visitorEventRepository.countByEventType("VIEW_ONLY_ACCESS")).thenReturn(4L);

        VisitorEvent sample = VisitorEvent.builder()
                .id(1L)
                .eventType("VIEW_ONLY_ACCESS")
                .referrerSource("LinkedIn")
                .location("Toronto, ON (Canada)")
                .deviceType("Desktop")
                .browser("Google Chrome")
                .operatingSystem("macOS")
                .path("/dashboard")
                .timestamp(Instant.now())
                .build();

        when(visitorEventRepository.findAllByOrderByTimestampDesc(any(Pageable.class)))
                .thenReturn(List.of(sample));

        VisitorMetricsSummaryResponse summary = visitorMetricsService.getMetricsSummary();

        assertNotNull(summary);
        assertEquals(15L, summary.getTotalVisits());
        assertEquals(10L, summary.getUniqueVisitors());
        assertEquals(4L, summary.getViewOnlyVisits());
        assertEquals(1, summary.getRecentVisits().size());
        assertEquals("LinkedIn", summary.getRecentVisits().get(0).getReferrerSource());
    }
}
