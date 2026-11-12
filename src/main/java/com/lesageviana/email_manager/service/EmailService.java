package com.lesageviana.email_manager.service;

import com.lesageviana.email_manager.agent.DispatcherAgent;
import com.lesageviana.email_manager.model.EmailMessage;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import jakarta.mail.*;
import jakarta.mail.event.MessageCountAdapter;
import jakarta.mail.event.MessageCountEvent;

import com.sun.mail.imap.IMAPFolder;

import java.util.Properties;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService implements Runnable {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    // Use a queue for sequential processing of incoming emails
    private static volatile BlockingQueue<EmailMessage> emailProcessingQueue = new LinkedBlockingQueue<>();

    @Value("${email-manager.imap.host}")
    private String imapHost;

    @Value("${email-manager.imap.port}")
    private String imapPort;

    @Value("${email-manager.imap.username}")
    private String imapUsername;

    @Value("${email-manager.imap.password}")
    private String imapPassword;

    @Value("${email-manager.inbox.folder}")
    private String inboxFolder;

    private static final OutgoingMailService outgoingMailService = new OutgoingMailService();

    @Autowired
    private DispatcherAgent dispatcherAgent;
    /**
     * Initializes the connection and starts listening when the service bean is created.
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing EmailService: Starting IMAP listener.");
        // Start the dedicated thread for listening and enqueuing messages
        new Thread(this::listenAndEnqueueMessages).start();
        // Start a separate consumer thread to process messages sequentially from the queue
        new Thread(this::processMessageQueue).start();
    }

    @Override
    public void run() {
        listenAndEnqueueMessages();
    }

    /**
     * The run method implements Runnable, allowing us to start continuous background listening.
     */
        /**
     * The run method is renamed to listenAndEnqueueMessages and handles continuous IMAP listening
     * and adding incoming messages to the shared queue for sequential processing.
     */
    public void listenAndEnqueueMessages() {
        Store store = null;
        IMAPFolder folder = null;

        try {
            store = getImapStore();
            folder = getFolderFromStore(store);

            while (!Thread.currentThread().isInterrupted()) {
                folder.idle();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during IMAP listening loop.", e);
        } finally {
             if (folder != null && folder.isOpen()) {
                 try {
                     folder.close();
                 } catch (MessagingException e) {
                     // Ignore
                 }
             }
             if (store != null && store.isConnected()) {
                 try {
                     store.close();
                 } catch (MessagingException e) {
                     // Ignore
                 }
             }
        }
    }

    /**
     * Dedicated consumer thread that processes messages sequentially from the queue.
     */
    private void processMessageQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Waits indefinitely until a message is available in the queue (blocking call)
                Message message = messageQueue.take();
                logger.info("Processing new email received from queue.");

                try {
                    processNewMessage(message);
                } catch (Exception e) {
                     logger.log(Level.SEVERE, "Failed to process message from queue.", e);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warning("Email processing thread interrupted.");
                break;
            }
        }
    }

    private Properties getImapProperties() {
		Properties props = new Properties();
        props.put("mail.store.api", "imaps");
        props.put("mail.imap.host", this.imapHost);
        props.put("mail.imap.port", this.imapPort);
		return props;
	}

    private Store getImapStore() throws Exception {
		Session session = Session.getInstance(getImapProperties());
		Store store = session.getStore("imaps");
		store.connect(this.imapHost, this.imapUsername, this.imapPassword);
		return store;
	}

    private IMAPFolder getFolderFromStore(Store store) throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getFolder(this.inboxFolder);
		folder.open(Folder.READ_ONLY);

        folder.addMessageCountListener(new MessageCountAdapter() {
            public void messagesAdded(MessageCountEvent event) {
                Message[] messages = event.getMessages();
                for (Message message : messages) {
                    try {
                        messageQueue.put(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
		return folder;
	}

    /**
     * This method would be called internally by a separate handler (or the IDLE mechanism)
     * when a new email is detected and processed.
     * @param message The newly fetched email content.
     */
    public void processNewMessage(Message message) throws MessagingException {
        try {
            String subject = message.getSubject();
            String sender;
            if (message.getFrom() != null && message.getFrom().length > 0) {
                sender = message.getFrom()[0].toString(); 
            } else {
                sender = "Null";
            }
            String[] allRecipients = Arrays.toString(message.getAllRecipients()).split("[\\[\\]]")[1].split(", ");
            Date date = message.getSentDate();
            String bodyText = message.getContent().toString();
            String messageId;
            String inReplyTo;

            if (message.getHeader("Message-Id") != null && message.getHeader("Message-Id").length > 0) {
                messageId = message.getHeader("Message-Id")[0]; 
            } else {
                messageId = "Null";
            }

            if (message.getHeader("In-Reply-To") != null && message.getHeader("In-Reply-To").length > 0) {
                inReplyTo = message.getHeader("In-Reply-To")[0]; 
            } else {
                inReplyTo = "Null";
            }

            // Map Message to EmailMessage model
            EmailMessage emailMessage = new EmailMessage(messageId, inReplyTo, sender, allRecipients, subject, date, bodyText, message);

            logger.info("Processed new email: From " + sender + ", Subject: " + subject);

            emailMessage.printEmail();


            String replyContent = dispatcherAgent.processEmail(emailMessage);

            System.out.println("Proposed reply: \n" + replyContent + "\nSend this reply ? (y/n)\n");

            boolean successReply;
            try (Scanner scanner = new Scanner(System.in)) {
                String confirmation = scanner.nextLine().trim().toLowerCase();
                if ("y".equals(confirmation)) {
                    successReply = outgoingMailService.sendEmailReply(emailMessage, replyContent, false);
                } else {
                    System.out.println("Sending cancelled by user.");
                    successReply = false;
                }
            }

            if (successReply == true) {
                System.out.println("Outgoing Mail Test Status: SUCCESS");
            } else {
                System.err.println("Outgoing Mail Test Status: FAILED (Check credentials/logs)");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing message.", e);
            throw new MessagingException("Failed to process email content", e);
        }
    }
}