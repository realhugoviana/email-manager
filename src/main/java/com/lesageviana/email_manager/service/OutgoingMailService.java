package com.lesageviana.email_manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 * Service responsible for sending outgoing emails using Spring's JavaMailSender.
 */
@Service 
public class OutgoingMailService {

    // We can optionally inject a default 'from' address if needed, 
    // but typically the config handles this via SMTP settings.
    @Value("${email-manager.smtp.host}")
    private String smtpHost;

    @Value("${email-manager.smtp.port}")
    private String smtpPort;

    @Value("${email-manager.smtp.username}")
    private String smtpUsername;

    @Value("${email-manager.smtp.password}")
    private String smtpPassword;


    /**
     * Sends a plain text email message to the specified recipient.
     * 
     * @param to The primary recipient's email address.
     * @param subject The subject line of the email.
     * @param bodyContent The plain text content of the email body.
     * @return true if sending was successful, false otherwise.
     */
    public boolean sendSimpleEmail(String to, String subject, String bodyContent) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(this.smtpUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(bodyContent);
            Transport.send(message);
            System.out.println("Successfully sent email to: " + to);
            return true;

        } catch (MessagingException e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
	
	private Session getEmailSession() {
		return Session.getInstance(getGmailProperties(), new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication(smtpUsername, smtpPassword);
		    }
		});
	}
	
	private Properties getGmailProperties() {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", this.smtpHost);
		prop.put("mail.smtp.port", this.smtpPort);
		prop.put("mail.smtp.ssl.trust", this.smtpHost);
		return prop;
	}
}