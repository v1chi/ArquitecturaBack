package com.team.socialnetwork.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${EMAIL_USER}")
    private String fromEmail;

    @Value("${API_BASE_URL:http://localhost:8080}")
    private String apiBaseUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private String loadTemplate(String name, String token) throws IOException {
        String templatePath = "mail/templates/" + name;
        ClassPathResource resource = new ClassPathResource(templatePath);
        // Read resource safely inside a JAR
        try (var is = resource.getInputStream()) {
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String url = apiBaseUrl + "/auth/" + token;
            return html.replace("{{LINK}}", url);
        }
    }

    public void sendConfirmationEmail(String to, String token) throws MessagingException, IOException {
        String html = loadTemplate("confirm-email.html", "confirm-email?token=" + token);
        sendEmail(to, "✅ Confirm your account", html);
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
