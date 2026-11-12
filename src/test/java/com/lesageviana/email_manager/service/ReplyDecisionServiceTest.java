package com.lesageviana.email_manager.service;

import com.lesageviana.email_manager.model.EmailMessage;
import com.lesageviana.email_manager.service.ReplyDecisionService.Decision;
import com.lesageviana.email_manager.service.ReplyDecisionService.Status;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ReplyDecisionServiceTest {

    private static EmailMessage email() {
        return new EmailMessage("<id@example.com>", "Null", "alice@example.com", new String[]{"me@example.com"}, "Hello", new Date(), "Body", null);
    }

    @Test
    void processingThreadBlocksUntilUiDecides() throws Exception {
        ReplyDecisionService service = new ReplyDecisionService();
        List<Status> seen = new CopyOnWriteArrayList<>();
        CountDownLatch awaiting = new CountDownLatch(1);
        service.addListener(s -> {
            seen.add(s.status());
            if (s.status() == Status.AWAITING_DECISION) awaiting.countDown();
        });

        AtomicReference<Decision> result = new AtomicReference<>();
        Thread processing = new Thread(() -> {
            service.emailReceived(email());
            try {
                result.set(service.awaitDecision("Proposed"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        processing.start();

        assertTrue(awaiting.await(2, TimeUnit.SECONDS));
        assertEquals("Proposed", service.getSnapshot().proposedReply());
        assertTrue(processing.isAlive(), "processing thread must wait for the decision");

        // First tab wins, second is ignored.
        assertTrue(service.decide(Decision.accept("Edited")));
        assertFalse(service.decide(Decision.reject()));

        processing.join(2000);
        assertTrue(result.get().accepted());
        assertEquals("Edited", result.get().replyText());
        assertEquals(List.of(Status.GENERATING, Status.AWAITING_DECISION), seen);
    }

    @Test
    void decideWithoutPendingEmailIsIgnored() {
        assertFalse(new ReplyDecisionService().decide(Decision.reject()));
    }
}
