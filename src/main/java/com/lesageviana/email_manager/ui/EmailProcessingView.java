package com.lesageviana.email_manager.ui;

import com.lesageviana.email_manager.model.EmailMessage;
import com.lesageviana.email_manager.service.ReplyDecisionService;
import com.lesageviana.email_manager.service.ReplyDecisionService.Decision;
import com.lesageviana.email_manager.service.ReplyDecisionService.Snapshot;
import com.lesageviana.email_manager.service.ReplyDecisionService.Status;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Shows the email currently processed by {@code EmailService#processNewMessage}, the reply
 * proposed by the agents, and lets the user accept (optionally after editing) or reject it.
 *
 * <p>Threading: state changes are produced on the email processing thread. Each open tab
 * registers a listener and re-renders inside {@link UI#access}, which runs on the tab's own
 * session lock and is pushed to the browser via {@code @Push}.</p>
 */
@Route("")
@PageTitle("Email Manager")
public class EmailProcessingView extends VerticalLayout {

    private final ReplyDecisionService decisionService;

    private final Badge statusBadge = new Badge();
    private final Span queueInfo = new Span();

    private final Span from = new Span();
    private final Span to = new Span();
    private final Span subject = new Span();
    private final Span date = new Span();
    private final Pre body = new Pre();
    private final Div emailPlaceholder = new Div("No email is being processed. Waiting for new messages…");
    private final Div emailDetails = new Div();

    private final ProgressBar generatingBar = new ProgressBar();
    private final Span generatingLabel = new Span("The agents are writing a reply…");
    private final TextArea replyArea = new TextArea();
    private final Button sendButton = new Button("Send reply");
    private final Button discardButton = new Button("Discard");
    private final Span resultDetail = new Span();

    private Runnable removeListener;
    /** Last snapshot rendered, used to avoid overwriting the user's edits on unrelated updates. */
    private Snapshot rendered;

    public EmailProcessingView(ReplyDecisionService decisionService) {
        this.decisionService = decisionService;

        setMaxWidth("960px");
        setWidthFull();
        getStyle().set("margin", "0 auto");
        setPadding(true);
        setSpacing(true);

        add(buildHeader(), buildEmailCard(), buildReplyCard());
    }

    // ------------------------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------------------------

    private HorizontalLayout buildHeader() {
        H1 title = new H1("Email Manager");
        title.addClassNames(LumoUtility.FontSize.XXLARGE, LumoUtility.Margin.NONE);
        queueInfo.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        HorizontalLayout right = new HorizontalLayout(queueInfo, statusBadge);
        right.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(title, right);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("flex-wrap", "wrap");
        return header;
    }

    private Div buildEmailCard() {
        Div grid = new Div(
                label("From"), from,
                label("To"), to,
                label("Subject"), subject,
                label("Date"), date);
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "max-content 1fr")
                .set("gap", "var(--lumo-space-xs) var(--lumo-space-m)")
                .set("overflow-wrap", "anywhere");
        subject.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        body.getStyle()
                .set("white-space", "pre-wrap")
                .set("overflow-wrap", "anywhere")
                .set("font-family", "inherit")
                .set("max-height", "360px")
                .set("overflow", "auto")
                .set("margin", "var(--lumo-space-m) 0 0 0")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        emailDetails.add(grid, body);
        emailPlaceholder.addClassNames(LumoUtility.TextColor.SECONDARY);

        return card("Current email", emailPlaceholder, emailDetails);
    }

    private Div buildReplyCard() {
        generatingBar.setIndeterminate(true);
        generatingLabel.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        replyArea.setWidthFull();
        replyArea.setMinHeight("220px");
        replyArea.setMaxHeight("480px");
        replyArea.setHelperText("You can edit the reply before sending it.");

        sendButton.addThemeVariants(ButtonVariant.PRIMARY);
        sendButton.addClickListener(e -> submit(Decision.accept(replyArea.getValue())));
        discardButton.addThemeVariants(ButtonVariant.ERROR);
        discardButton.addClickListener(e -> submit(Decision.reject()));

        HorizontalLayout actions = new HorizontalLayout(sendButton, discardButton);
        resultDetail.addClassNames(LumoUtility.FontSize.SMALL);

        return card("Proposed reply", generatingLabel, generatingBar, replyArea, actions, resultDetail);
    }

    private static Span label(String text) {
        Span s = new Span(text);
        s.addClassNames(LumoUtility.TextColor.SECONDARY);
        return s;
    }

    private static Div card(String title, com.vaadin.flow.component.Component... content) {
        H2 h = new H2(title);
        h.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Top.NONE, LumoUtility.Margin.Bottom.MEDIUM);
        Div card = new Div(h);
        card.add(content);
        card.setWidthFull();
        card.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "var(--lumo-space-l)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("background", "var(--lumo-base-color)");
        return card;
    }

    // ------------------------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------------------------

    private void submit(Decision decision) {
        sendButton.setEnabled(false);
        discardButton.setEnabled(false);
        if (!decisionService.decide(decision)) {
            Notification.show("This email was already handled (maybe from another tab).", 4000,
                    Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.WARNING);
        }
        // The resulting state change (SENDING / REJECTED) arrives through the listener.
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        rendered = null;
        render(decisionService.getSnapshot());
        // Called on the email processing thread: hop onto this tab's UI thread.
        removeListener = decisionService.addListener(snapshot -> ui.access(() -> render(snapshot)));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (removeListener != null) {
            removeListener.run();
            removeListener = null;
        }
        super.onDetach(detachEvent);
    }

    private void render(Snapshot s) {
        Snapshot previous = rendered;
        rendered = s;

        // Header
        statusBadge.setText(statusText(s.status()));
        statusBadge.removeThemeVariants(BadgeVariant.SUCCESS, BadgeVariant.ERROR, BadgeVariant.WARNING, BadgeVariant.CONTRAST);
        BadgeVariant variant = statusVariant(s.status());
        if (variant != null) statusBadge.addThemeVariants(variant);
        queueInfo.setText(s.queuedEmails() == 0 ? "" : s.queuedEmails() + " email(s) waiting in queue");

        // Email
        EmailMessage email = s.email();
        emailPlaceholder.setVisible(email == null);
        emailDetails.setVisible(email != null);
        if (email != null && (previous == null || previous.email() != email)) {
            from.setText(nullSafe(email.getSender()));
            to.setText(email.getAllRecipients() == null ? "" : String.join(", ", email.getAllRecipients()));
            subject.setText(nullSafe(email.getSubject()));
            date.setText(email.getReceivedAt() == null ? ""
                    : new SimpleDateFormat("EEE d MMM yyyy, HH:mm").format(email.getReceivedAt()));
            body.setText(nullSafe(email.getBody()));
        }

        // Reply
        boolean generating = s.status() == Status.GENERATING;
        boolean awaiting = s.status() == Status.AWAITING_DECISION;
        generatingBar.setVisible(generating);
        generatingLabel.setVisible(generating);
        replyArea.setVisible(s.proposedReply() != null);
        sendButton.setVisible(s.proposedReply() != null);
        discardButton.setVisible(s.proposedReply() != null);

        // Only (re)load the text when the proposal itself changes, so a queue-size update
        // doesn't wipe what the user is typing.
        boolean newProposal = previous == null || !Objects.equals(previous.proposedReply(), s.proposedReply());
        if (s.proposedReply() != null && newProposal) {
            replyArea.setValue(s.proposedReply());
        }
        replyArea.setReadOnly(!awaiting);
        sendButton.setEnabled(awaiting);
        discardButton.setEnabled(awaiting);

        resultDetail.setText(nullSafe(s.detail()));
        resultDetail.getStyle().set("color", switch (s.status()) {
            case SENT -> "var(--lumo-success-text-color)";
            case FAILED -> "var(--lumo-error-text-color)";
            default -> "var(--lumo-secondary-text-color)";
        });

        if (awaiting && (previous == null || previous.status() != Status.AWAITING_DECISION)) {
            Notification.show("A reply is ready for review.", 3000, Notification.Position.BOTTOM_START);
        }
    }

    private static String statusText(Status status) {
        return switch (status) {
            case IDLE -> "Waiting for emails";
            case GENERATING -> "Generating reply";
            case AWAITING_DECISION -> "Awaiting your decision";
            case SENDING -> "Sending";
            case SENT -> "Sent";
            case REJECTED -> "Discarded";
            case FAILED -> "Failed";
        };
    }

    private static BadgeVariant statusVariant(Status status) {
        return switch (status) {
            case AWAITING_DECISION -> BadgeVariant.WARNING;
            case SENT -> BadgeVariant.SUCCESS;
            case FAILED -> BadgeVariant.ERROR;
            case IDLE, REJECTED -> BadgeVariant.CONTRAST;
            default -> null;
        };
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
