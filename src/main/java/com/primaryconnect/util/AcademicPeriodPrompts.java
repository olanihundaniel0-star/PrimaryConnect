package com.primaryconnect.util;

import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;

import java.util.Locale;
import java.util.Scanner;

/**
 * Shared prompt helpers for structured term and session input.
 */
public final class AcademicPeriodPrompts {
    private AcademicPeriodPrompts() {
    }

    public static AcademicTerm promptForTerm(Scanner scanner, String prompt) {
        while (true) {
            printTermOptions();
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
                continue;
            }

            try {
                return AcademicTerm.fromChoice(Integer.parseInt(input));
            } catch (NumberFormatException ignored) {
                try {
                    return AcademicTerm.fromLabel(input);
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    public static AcademicSession promptForSession(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be blank.");
                continue;
            }

            try {
                return AcademicSession.parse(input);
            } catch (IllegalArgumentException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    private static void printTermOptions() {
        System.out.println("Available terms:");
        AcademicTerm[] terms = AcademicTerm.values();
        for (int index = 0; index < terms.length; index++) {
            System.out.printf(Locale.ROOT, "%d. %s%n", index + 1, terms[index].getDisplayName());
        }
    }
}
