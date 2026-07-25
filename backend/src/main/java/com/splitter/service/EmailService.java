package com.splitter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends email via Resend's HTTP API instead of raw SMTP. Most cloud hosts
 * (Railway included) block outbound SMTP entirely as an anti-spam measure —
 * an HTTP API call is just a normal HTTPS request, so it isn't affected.
 *
 * When app.mail.enabled=false (the default), no real email is sent — instead
 * the link is logged at INFO level so the whole verification/reset flow can
 * be exercised end-to-end without an API key configured yet.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestClient restClient = RestClient.create();

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String from;

    @Value("${app.mail.resend-api-key:}")
    private String resendApiKey;

    public void sendVerificationEmail(String toEmail, String link) {
        send(toEmail, "Verify your SplitLedger email",
                "Welcome to SplitLedger! Click the link below to verify your email address:\n\n" + link +
                        "\n\nIf you didn't create this account, you can ignore this email.");
    }

    public void sendPasswordResetEmail(String toEmail, String link) {
        send(toEmail, "Reset your SplitLedger password",
                "We received a request to reset your password. Click the link below (valid for 30 minutes):\n\n" + link +
                        "\n\nIf you didn't request this, you can safely ignore this email.");
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("=== [DEV MODE - email not actually sent] ===\nTo: {}\nSubject: {}\n{}\n===============================================",
                    to, subject, body);
            return;
        }

        restClient.post()
                .uri(RESEND_API_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", from,
                        "to", List.of(to),
                        "subject", subject,
                        "text", body
                ))
                .retrieve()
                .toBodilessEntity();
    }
}