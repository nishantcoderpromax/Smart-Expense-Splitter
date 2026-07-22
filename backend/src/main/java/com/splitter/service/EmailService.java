package com.splitter.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
 
/**
 * When app.mail.enabled=false (the default), no real email is sent — instead
 * the link is logged at INFO level so the whole verification/reset flow can
 * be exercised end-to-end without setting up real SMTP credentials first.
 * Flip app.mail.enabled=true once real spring.mail.* credentials are set.
 */
@Service
@RequiredArgsConstructor
public class EmailService {
 
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
 
    private final JavaMailSender mailSender;
 
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;
 
    @Value("${app.mail.from:no-reply@splitledger.local}")
    private String from;
 
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
 
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}