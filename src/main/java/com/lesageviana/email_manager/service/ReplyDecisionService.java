package com.lesageviana.email_manager.service;

import com.lesageviana.email_manager.model.EmailMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe bridge between the email processing thread (EmailService consumer thread)
 * and the web UI (Vaadin request / push threads).
 *
 * <ul>
 *   <li>The processing thread publishes state changes (new email, proposed reply, result)
 *       and blocks in {@link #awaitDecision(String)} until a user decides.</li>
 *   <li>UI sessions subscribe with {@link #addListener(Consumer)} and are notified on every
 *       change. Listeners are invoked on the publishing thread, so UI code must hop onto its
 *       own UI thread (Vaadin {@code UI.access}).</li>
 *   <li>A UI submits the decision with {@link #decide(Decision)}. Only the first decision for
 *       the current email wins, even if several browser tabs are open.</li>
 * </ul>
 */
@Service
public class ReplyDecisionService {

    private static final Logger logger = Logger.getLogger(ReplyDecisionService.class.getName());

    public enum Status {
        /** Waiting for a new email to arrive. */
        IDLE,
        /** An email was received and the agents are generating a reply. */
        GENERATING,
        /** A reply was proposed and is waiting for the user's decision. */
        AWAITING_DECISION,
        /** The user accepted; the reply is being sent. */
        SENDING,
        /** The reply was sent successfully. */
        SENT,
        /** The user rejected the reply. */
        REJECTED,
        /** Generating or sending failed. */
        FAILED
    }

    /** The user's decision; {@code replyText} may differ from the proposal if the user edited it. */
    public record Decision(boolean accepted, String replyText) {
        public static Decision accept(String replyText) { return new Decision(true, replyText); }
        public static Decision reject() { return new Decision(false, null); }
    }

    /** Immutable snapshot of the current processing state, safe to hand to any thread. */
    public record Snapshot(Status status, EmailMessage email, String proposedReply, String detail, int queuedEmails) {}

    private final List<Consumer<Snapshot>> listeners = new CopyOnWriteArrayList<>();

    private volatile Snapshot snapshot = new Snapshot(Status.IDLE, null, null, null, 0);

    /** Decision for the email currently awaiting one; null when nothing is pending. */
    private volatile CompletableFuture<Decision> pendingDecision;

    // ---------------------------------------------------------------------------------------
    // Called from the email processing side
    // ---------------------------------------------------------------------------------------

    public void emailReceived(EmailMessage email) {
        mutate(s -> new Snapshot(Status.GENERATING, email, null, null, s.queuedEmails()));
    }

    /**
     * Publishes the proposed reply and blocks the calling (processing) thread until a UI
     * submits a decision.
     */
    public Decision awaitDecision(String proposedReply) throws InterruptedException {
        CompletableFuture<Decision> future = new CompletableFuture<>();
        pendingDecision = future;
        mutate(s -> new Snapshot(Status.AWAITING_DECISION, s.email(), proposedReply, null, s.queuedEmails()));
        try {
            return future.get();
        } catch (ExecutionException e) {
            // Never completed exceptionally; treat defensively as a rejection.
            return Decision.reject();
        } finally {
            pendingDecision = null;
        }
    }

    /** Marks the reply as being sent; {@code finalReply} is the (possibly edited) text. */
    public void sending(String finalReply) {
        mutate(s -> new Snapshot(Status.SENDING, s.email(), finalReply, null, s.queuedEmails()));
    }

    public void finished(Status status, String detail) {
        mutate(s -> new Snapshot(status, s.email(), s.proposedReply(), detail, s.queuedEmails()));
    }

    public void queueSizeChanged(int size) {
        mutate(s -> new Snapshot(s.status(), s.email(), s.proposedReply(), s.detail(), size));
    }

    // ---------------------------------------------------------------------------------------
    // Called from the UI side
    // ---------------------------------------------------------------------------------------

    /**
     * Submits the user's decision for the email currently awaiting one.
     *
     * @return true if the decision was taken into account, false if there was nothing to
     *         decide (e.g. another tab already decided).
     */
    public boolean decide(Decision decision) {
        CompletableFuture<Decision> future = pendingDecision;
        return future != null && future.complete(decision);
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    /** Registers a listener; returns a handle that removes it. */
    public Runnable addListener(Consumer<Snapshot> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    // ---------------------------------------------------------------------------------------

    /**
     * Atomically derives the new snapshot from the current one and notifies listeners.
     * Listeners only enqueue UI work (UI.access), so holding the lock here cannot deadlock.
     */
    private synchronized void mutate(UnaryOperator<Snapshot> change) {
        Snapshot newSnapshot = change.apply(snapshot);
        snapshot = newSnapshot;
        for (Consumer<Snapshot> listener : listeners) {
            try {
                listener.accept(newSnapshot);
            } catch (Exception e) {
                logger.log(Level.WARNING, "UI listener failed", e);
            }
        }
    }
}
