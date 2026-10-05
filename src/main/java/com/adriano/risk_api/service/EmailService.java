package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.AccessRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.access-request-recipient:adrianoreck@gmail.com}")
    private String recipient;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAccessRequestNotification(AccessRequest request) {
        String requesterName = (request.getName() != null && !request.getName().isBlank())
                ? request.getName().trim()
                : "Not provided";

        String subject = "[Risk API] Access Request: " + request.getEmail();
        String body = String.format(
                "A new user has submitted an access request for the Risk Assessment Portal:\n\n" +
                "• Requester Email: %s\n" +
                "• Requester Name:  %s\n" +
                "• Reason:\n%s\n\n" +
                "• Submitted At:    %s\n",
                request.getEmail().trim(),
                requesterName,
                request.getReason().trim(),
                Instant.now()
        );

        if (mailSender != null && fromAddress != null && !fromAddress.isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(recipient);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Access request email sent successfully to {}", recipient);
                return;
            } catch (Exception ex) {
                log.error("Failed to send access request email to {}: {}", recipient, ex.getMessage(), ex);
            }
        } else {
            log.info("SMTP sender is not configured or username is blank. Access request logged for notification:\nRecipient: {}\nSubject: {}\n{}",
                    recipient, subject, body);
        }
    }

    public String getRecipient() {
        return recipient;
    }

}
