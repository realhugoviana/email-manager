package com.lesageviana.email_manager.service;

import com.lesageviana.email_manager.model.EmailMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;

import com.sun.mail.imap.IMAPFolder;

import java.util.Properties;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService implements Runnable {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());
    
    // Use AtomicReference to safely store the last message from multiple threads
    private final static AtomicReference<EmailMessage> lastReceivedEmail = new AtomicReference<>(null);

    /**
     * Initializes the connection and starts listening when the service bean is created.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing EmailService: Starting IMAP listener.");
        // Run the listener logic in a separate thread to avoid blocking the Spring context
        new Thread(this).start();
    }

    /**
     * The run method implements Runnable, allowing us to start continuous background listening.
     */
    @Override
    public void run() {
        Store store = null;
        IMAPFolder folder = null;

        try {
            store = getImapStore();
            folder = getFolderFromStore(store);

            while (!Thread.currentThread().isInterrupted()) {
                folder.idle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Properties getImapProperties() {
		Properties props = new Properties();
        props.put("mail.store.api", "imaps"); 
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.imap.port", "993");
		return props;
	}

    private static Store getImapStore() throws Exception {
		Session session = Session.getInstance(getImapProperties());
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", "testlesageviana@gmail.com", "ugpk zijz hqkj ttxj");
		return store;
	}

    private static IMAPFolder getFolderFromStore(Store store) throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);

        folder.addMessageCountListener(new MessageCountAdapter() {
            public void messagesAdded(MessageCountEvent event) {
                Message[] messages = event.getMessages();
                for (Message message : messages) {
                    try {
                        processNewMessage(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
		return folder;
	}

    /**
     * Called by other parts of the application to retrieve the last processed email message.
     * @return The most recently received EmailMessage, or null if none.
     */
    public EmailMessage getLastReceivedEmail() {
        return lastReceivedEmail.get();
    }

    public static void printLastReceivedEmail() {
        lastReceivedEmail.get().printEmail();
    }

    /**
     * This method would be called internally by a separate handler (or the IDLE mechanism) 
     * when a new email is detected and processed.
     * @param message The newly fetched email content.
     */
    public static void processNewMessage(Message message) throws MessagingException {
        try {
            String subject = message.getSubject();
            String sender = message.getFrom()[0].toString();
            Date date = message.getSentDate();
            String bodyText = message.getContent().toString();

            // Map Message to EmailMessage model
            EmailMessage emailMessage = new EmailMessage(sender, subject, date, bodyText);
            
            logger.info("Processed new email: From " + sender + ", Subject: " + subject);

            // Update the shared reference
            lastReceivedEmail.set(emailMessage);
            printLastReceivedEmail();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing message.", e);
            throw new MessagingException("Failed to process email content", e);
        }
    }
}