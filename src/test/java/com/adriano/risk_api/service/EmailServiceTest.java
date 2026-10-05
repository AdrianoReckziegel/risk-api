package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.AccessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "recipient", "adrianoreck@gmail.com");
        ReflectionTestUtils.setField(emailService, "fromAddress", "sender@example.com");
    }

    @Test
    void shouldSendEmailWhenMailSenderConfigured() {
        AccessRequest request = AccessRequest.builder()
                .email("requester@test.com")
                .name("Jane Requester")
                .reason("Need access for compliance review")
                .build();

        emailService.sendAccessRequestNotification(request);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertNotNull(sent.getTo());
        assertEquals("adrianoreck@gmail.com", sent.getTo()[0]);
        assertTrue(sent.getSubject().contains("requester@test.com"));
        assertTrue(sent.getText().contains("Jane Requester"));
        assertTrue(sent.getText().contains("Need access for compliance review"));
    }

    @Test
    void shouldHandleNullMailSenderGracefullyWithoutException() {
        EmailService serviceWithoutSender = new EmailService(null);
        ReflectionTestUtils.setField(serviceWithoutSender, "recipient", "adrianoreck@gmail.com");

        AccessRequest request = AccessRequest.builder()
                .email("user@test.com")
                .reason("Just testing")
                .build();

        assertDoesNotThrow(() -> serviceWithoutSender.sendAccessRequestNotification(request));
    }
}
