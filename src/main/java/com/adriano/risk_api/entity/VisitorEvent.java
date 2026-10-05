package com.adriano.risk_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "visitor_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(length = 64)
    private String clientIp;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 64)
    private String browser;

    @Column(length = 64)
    private String operatingSystem;

    @Column(length = 32)
    private String deviceType;

    @Column(length = 512)
    private String referrer;

    @Column(length = 64)
    private String referrerSource;

    @Column(length = 128)
    private String location;

    @Column(length = 64)
    private String timezone;

    @Column(length = 32)
    private String language;

    @Column(length = 32)
    private String screenResolution;

    @Column(length = 128)
    private String path;

    @Column(length = 64)
    private String sessionId;

    @Column(nullable = false)
    private Instant timestamp;

}
