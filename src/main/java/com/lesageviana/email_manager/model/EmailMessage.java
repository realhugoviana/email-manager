package com.lesageviana.email_manager.model;

import java.util.Arrays;
import java.util.Date;

import jakarta.mail.Message;

public class EmailMessage {
    private String sender;
    private String[] allRecipients;
    private String subject;
    private String body;
    private Date receivedAt;
    private Message originalMessage;

    public EmailMessage(String sender, String[] allRecipients, String subject, Date date, String body, Message originalMessage) {
        this.sender = sender;
        this.allRecipients = allRecipients;
        this.subject = subject;
        this.body = body;
        this.receivedAt = date;
        this.originalMessage = originalMessage;
    }

    public String getSender() { return sender; }
    public String[] getAllRecipients() { return allRecipients; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Date getReceivedAt() { return receivedAt; }
    public Message getOriginalMessage() { return originalMessage; }

    @Override
    public String toString() {
        return "EmailMessage{\n" +
                "sender=" + sender + '\n' +
                "allRecipients=" + Arrays.toString(allRecipients) + '\n' +
                "subject=" + subject + '\n' +
                "receivedAt=" + receivedAt + '\n' +
                '}';
    }
    
    public void printEmail() {
        System.out.println(toString());
    }
}