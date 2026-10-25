package com.lesageviana.email_manager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.lesageviana.email_manager.service.LMService;
import com.lesageviana.email_manager.service.OutgoingMailService;

@SpringBootApplication
public class EmailManagerApplication implements CommandLineRunner {

    private final LMService chatService;
    private final OutgoingMailService outgoingMailService;

    public EmailManagerApplication(OutgoingMailService outgoingMailService, LMService chatService) {
        this.outgoingMailService = outgoingMailService;
        this.chatService = chatService;
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
        System.out.println("--- Running LM studio Test ---");
        // NOTE: Update these details with actual testing data before deployment!

        try {
            String answer = chatService.chat("Salut, ça va ?");

            System.out.println(answer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

