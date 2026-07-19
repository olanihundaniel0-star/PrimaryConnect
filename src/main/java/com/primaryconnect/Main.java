package com.primaryconnect;

import com.primaryconnect.data.DatabaseManager;

/**
 * Provides the console entry point for the PrimaryConnect application, initialising the database layer and routing users to role-specific menus after login.
 */
public class Main {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        System.out.println("PrimaryConnect startup placeholder.");
        // TODO: show login prompt and route to the appropriate role menu.
        databaseManager.initialize();
    }
}
