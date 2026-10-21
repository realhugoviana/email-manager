package com.lesageviana.email_manager.model;

import java.util.Date;

public class EmailMessage {
    private String sender;
    private String subject;
    private String body;
    private Date receivedAt;

    public EmailMessage(String sender, String subject, Date date, String body) {
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.receivedAt = date;
    }

    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public Date getReceivedAt() { return receivedAt; }

    @Override
    public String toString() {
        return "EmailMessage{" +
                "sender='" + sender + '\'' +
                ", subject='" + subject + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
    
    public void printEmail() {
        System.out.println(toString());
    }
}