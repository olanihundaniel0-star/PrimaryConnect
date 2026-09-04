package com.primaryconnect.model;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Represents the supported school terms in the current demo scope.
 */
public enum AcademicTerm {
    FIRST("First Term"),
    SECOND("Second Term"),
    THIRD("Third Term");

    private final String displayName;

    AcademicTerm(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AcademicTerm fromChoice(int choice) {
        return switch (choice) {
            case 1 -> FIRST;
            case 2 -> SECOND;
            case 3 -> THIRD;
            default -> throw new IllegalArgumentException("Invalid term selection: " + choice + ".");
        };
    }

    public static AcademicTerm fromLabel(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Term cannot be blank.");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', ' ');
        normalized = normalized.replaceAll("\\s+", " ");

        return switch (normalized) {
            case "1", "1ST", "1ST TERM", "FIRST", "FIRST TERM" -> FIRST;
            case "2", "2ND", "2ND TERM", "SECOND", "SECOND TERM" -> SECOND;
            case "3", "3RD", "3RD TERM", "THIRD", "THIRD TERM" -> THIRD;
            default -> throw new IllegalArgumentException(
                    "Unsupported term: " + value + ". Expected First Term, Second Term, or Third Term."
            );
        };
    }

    public static List<String> displayNames() {
        return Arrays.stream(values())
                .map(AcademicTerm::getDisplayName)
                .toList();
    }
}
