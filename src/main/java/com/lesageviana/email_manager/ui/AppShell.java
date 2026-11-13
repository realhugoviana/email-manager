package com.lesageviana.email_manager.ui;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.lumo.Lumo;

/**
 * Application-wide Vaadin configuration.
 * {@link Push} is required so the email processing thread can update open browser tabs
 * (new email arrived, reply proposed, reply sent) without the user refreshing.
 */
@Push
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
public class AppShell implements AppShellConfigurator {
}
