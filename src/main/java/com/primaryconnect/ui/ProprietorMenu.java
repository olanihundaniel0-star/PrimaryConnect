package com.primaryconnect.ui;

import java.util.Scanner;

/**
 * Provides the console menu for the school proprietor, exposing school-wide reporting, attendance summaries, and synchronization actions.
 */
public class ProprietorMenu {
    private final Scanner scanner;

    public ProprietorMenu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the proprietor menu and processes user input until logout is selected.
     */
    public void show() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readChoice();

            switch (choice) {
                case 1:
                    viewAttendanceSummary();
                    break;
                case 2:
                    viewClassRankings();
                    break;
                case 3:
                    triggerDataSync();
                    break;
                case 4:
                    logout();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n=== Proprietor Menu ===");
        System.out.println("1. View school-wide attendance summary");
        System.out.println("2. View class rankings and reports");
        System.out.println("3. Trigger data sync");
        System.out.println("4. Log out");
        System.out.print("Enter your choice: ");
    }

    private int readChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void viewAttendanceSummary() {
        System.out.println("\n[Not yet implemented: View school-wide attendance summary]");
    }

    private void viewClassRankings() {
        System.out.println("\n[Not yet implemented: View class rankings and reports]");
    }

    private void triggerDataSync() {
        System.out.println("\n[Not yet implemented: Trigger data sync]");
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }
}
