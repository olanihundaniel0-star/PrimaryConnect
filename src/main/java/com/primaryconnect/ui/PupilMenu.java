package com.primaryconnect.ui;

import java.util.Scanner;

/**
 * Provides the console menu for a pupil, allowing practice exercises and access to the learner's own report.
 */
public class PupilMenu {
    private final Scanner scanner;

    public PupilMenu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the pupil menu and processes user input until logout is selected.
     */
    public void show() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readChoice();

            switch (choice) {
                case 1:
                    takeExercise();
                    break;
                case 2:
                    viewAttendanceRecord();
                    break;
                case 3:
                    viewReportCard();
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
        System.out.println("\n=== Pupil Menu ===");
        System.out.println("1. Take an exercise");
        System.out.println("2. View my own attendance record");
        System.out.println("3. View my report card");
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

    private void takeExercise() {
        System.out.println("\n[Not yet implemented: Take an exercise]");
    }

    private void viewAttendanceRecord() {
        System.out.println("\n[Not yet implemented: View my own attendance record]");
    }

    private void viewReportCard() {
        System.out.println("\n[Not yet implemented: View my report card]");
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }
}
