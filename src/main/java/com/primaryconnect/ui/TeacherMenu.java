package com.primaryconnect.ui;

import java.util.Scanner;

/**
 * Provides the console menu for a teacher, enabling score entry, attendance marking, and media lesson launch actions.
 */
public class TeacherMenu {
    private final Scanner scanner;

    public TeacherMenu() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the teacher menu and processes user input until logout is selected.
     */
    public void show() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readChoice();

            switch (choice) {
                case 1:
                    markAttendance();
                    break;
                case 2:
                    enterScores();
                    break;
                case 3:
                    launchMediaLesson();
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
        System.out.println("\n=== Teacher Menu ===");
        System.out.println("1. Mark attendance for a class");
        System.out.println("2. Enter test/exam scores");
        System.out.println("3. Launch a media lesson");
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

    private void markAttendance() {
        System.out.println("\n[Not yet implemented: Mark attendance for a class]");
    }

    private void enterScores() {
        System.out.println("\n[Not yet implemented: Enter test/exam scores]");
    }

    private void launchMediaLesson() {
        System.out.println("\n[Not yet implemented: Launch a media lesson]");
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }
}
