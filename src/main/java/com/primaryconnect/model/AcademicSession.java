package com.primaryconnect.model;

/**
 * Represents an academic session such as 2025/2026.
 */
public record AcademicSession(int startYear, int endYear) {
    public AcademicSession {
        if (endYear != startYear + 1) {
            throw new IllegalArgumentException(
                    "Invalid academic session: " + startYear + "/" + endYear + ". Expected consecutive years."
            );
        }
    }

    public static AcademicSession parse(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Academic session cannot be blank.");
        }

        String trimmed = value.trim();
        if (!trimmed.matches("\\d{4}/\\d{4}")) {
            throw new IllegalArgumentException(
                    "Unsupported academic session: " + value + ". Expected format YYYY/YYYY."
            );
        }

        int startYear = Integer.parseInt(trimmed.substring(0, 4));
        int endYear = Integer.parseInt(trimmed.substring(5));
        return new AcademicSession(startYear, endYear);
    }

    @Override
    public String toString() {
        return startYear + "/" + endYear;
    }
}
