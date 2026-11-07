package com.lesageviana.email_manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.lesageviana.email_manager.model.EmailMessage;

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
    public boolean sendEmailReply(EmailMessage message, String bodyContent, boolean replyToAll) {
        try {
            Message originalMessage = message.getOriginalMessage();

            Message reply = new MimeMessage(getEmailSession());

            reply.setFrom(new InternetAddress("testlesageviana@gmail.com"));

            reply.setRecipient(Message.RecipientType.TO, originalMessage.getFrom()[0]);
            if (replyToAll == true) {
                reply.addRecipients(Message.RecipientType.CC, originalMessage.getRecipients(Message.RecipientType.CC));
                reply.addRecipients(Message.RecipientType.BCC, originalMessage.getRecipients(Message.RecipientType.BCC));
            }

            String subject = originalMessage.getSubject();
            if (subject != null) {
                if (!subject.regionMatches(true, 0, "Re: ", 0, 4))
                    subject = "Re: " + subject;
                reply.setHeader("Subject", subject);
            }

            reply.setText(bodyContent);
            
            String[] messageId = originalMessage.getHeader("Message-Id");
            if (messageId != null && messageId.length > 0) {
                reply.setHeader("In-Reply-To", messageId[0]);
            }

            String[] references = originalMessage.getHeader("References");
            if (references != null && references.length > 0) {
                references = originalMessage.getHeader("In-Reply-To");
            }
            if (messageId != null) {
                if (references != null && references.length > 0) {
                    reply.setHeader("References", references + " " + messageId[0]);
                } else {
                    reply.setHeader("References", messageId[0]);
                }
            }

            Transport.send(reply);

            System.out.println("Successfully sent email");
            return true;

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
	
	private Session getEmailSession() {
		return Session.getInstance(getGmailProperties(), new Authenticator() {
		    protected PasswordAuthentication getPasswordAuthentication() {
		        return new PasswordAuthentication("testlesageviana@gmail.com", "ugpk zijz hqkj ttxj");
		    }
		});
	}
	
	private Properties getGmailProperties() {
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		return prop;
	}
}