package com.team.socialnetwork.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;

    @Value("${EMAIL_USER}")
    private String fromEmail;

    @Value("${API_BASE_URL:http://localhost:8080}")
    private String apiBaseUrl;

    @Value("${frontend.base.url:http://localhost:5173}")
    private String frontendBaseUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String loadTemplate(String name, String link) throws IOException {
        String templatePath = "mail/templates/" + name;
        ClassPathResource resource = new ClassPathResource(templatePath);
        // Read resource safely inside a JAR
        try (var is = resource.getInputStream()) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Log the URL for local development to easily copy the token/link
            log.info("Mail link ({}): {}", name, link);
            return html.replace("{{LINK}}", link);
        }
    }

    public void sendConfirmationEmail(String to, String token) throws MessagingException, IOException {
        // For email confirmation, we use the frontend URL so users can confirm their account in the UI
        String confirmationUrl = frontendBaseUrl + "/confirm-email?token=" + token;
        String html = loadTemplate("confirm-email.html", confirmationUrl);
        sendEmail(to, "✅ Confirm your account", html);
    }

    public void sendPasswordReset(String to, String token) throws MessagingException, IOException {
        // For password reset, we use the frontend URL so users can reset their password in the UI
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
        String html = loadTemplate("reset-password.html", resetUrl);
        sendEmail(to, "🔐 Reset your SocialNetwork password", html);
    }

    private void sendEmail(String to, String subject, String html) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
