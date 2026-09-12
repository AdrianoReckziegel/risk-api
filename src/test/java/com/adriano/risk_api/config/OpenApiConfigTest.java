package com.adriano.risk_api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void usesConfiguredApplicationVersion() {
        OpenApiConfig config = new OpenApiConfig("1.0.1");

        assertEquals("1.0.1", config.riskApiOpenAPI().getInfo().getVersion());
    }
}
