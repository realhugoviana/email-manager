package com.lesageviana.email_manager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lesageviana.email_manager.service.OutgoingMailService;

@SpringBootApplication
public class EmailManagerApplication implements CommandLineRunner {

    private final OutgoingMailService outgoingMailService;

    public EmailManagerApplication(OutgoingMailService outgoingMailService) {
        this.outgoingMailService = outgoingMailService;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmailManagerApplication.class, args);
    }

    /**
     * Executes code after the application context is loaded. 
     * This method serves as a simple test runner for outgoing mail capability.
     */
    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Running Outgoing Mail Test ---");
        // NOTE: Update these details with actual testing data before deployment!
        boolean success = outgoingMailService.sendSimpleEmail(
                "hugo.viana@etu.univ-cotedazur.fr", 
                "Salut", 
                "This is a test message to verify the sending functionality.\n\nIf you received this, the SMTP configuration and service are working correctly."
        );

        if (success) {
            System.out.println("Outgoing Mail Test Status: SUCCESS");
        } else {
            System.err.println("Outgoing Mail Test Status: FAILED (Check credentials/logs)");
        }
    }
}

