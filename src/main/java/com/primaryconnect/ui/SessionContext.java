package com.primaryconnect.ui;

import com.primaryconnect.model.User;

import java.util.Set;

/**
 * Stores active session information for the current application run.
 */
public final class SessionContext {
    private static final String DEFAULT_LANGUAGE = "ENGLISH";
    private static final Set<String> ALLOWED_LANGUAGES = Set.of("ENGLISH", "YORUBA", "IGBO", "HAUSA");

    private static final SessionContext INSTANCE = new SessionContext();

    private User currentUser;
    private String selectedLanguage = DEFAULT_LANGUAGE;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        return INSTANCE;
    }

    public void login(User user) {
        currentUser = user;
    }

    public void logout() {
        currentUser = null;
        selectedLanguage = DEFAULT_LANGUAGE;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentRole() {
        if (currentUser == null) {
            throw new IllegalStateException("Cannot get current role because no user is logged in.");
        }

        return currentUser.getRole();
    }

    public void setLanguage(String language) {
        if (!ALLOWED_LANGUAGES.contains(language)) {
            throw new IllegalArgumentException(
                    "Unsupported language: " + language + ". Expected one of " + ALLOWED_LANGUAGES + "."
            );
        }

        selectedLanguage = language;
    }

    public String getLanguage() {
        return selectedLanguage;
    }
}
