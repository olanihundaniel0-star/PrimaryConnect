package com.primaryconnect.ui;

import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SubjectDAO;
import com.primaryconnect.model.Pupil;
import com.primaryconnect.model.Score;
import com.primaryconnect.model.Subject;
import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;
import com.primaryconnect.service.AttendanceEngine;
import com.primaryconnect.service.GradingEngine;
import com.primaryconnect.service.SyncExporter;
import com.primaryconnect.service.SyncImporter;
import com.primaryconnect.util.AcademicPeriodPrompts;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Provides the console menu for the school proprietor, exposing school-wide reporting, attendance summaries, and synchronization actions.
 */
public class ProprietorMenu {
    private static final String PRIMARY_FIVE = "Primary 5";

    private final Scanner scanner;
    private final AttendanceEngine attendanceEngine;
    private final GradingEngine gradingEngine;
    private final SubjectDAO subjectDAO;
    private final PupilDAO pupilDAO;
    private final ScoreDAO scoreDAO;
    private final SyncExporter syncExporter;
    private final SyncImporter syncImporter;

    public ProprietorMenu() {
        this.scanner = new Scanner(System.in);
        this.attendanceEngine = new AttendanceEngine();
        this.gradingEngine = new GradingEngine();
        this.subjectDAO = new SubjectDAO();
        this.pupilDAO = new PupilDAO();
        this.scoreDAO = new ScoreDAO();
        this.syncExporter = new SyncExporter();
        this.syncImporter = new SyncImporter();
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
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void viewAttendanceSummary() {
        System.out.println("\n=== School-Wide Attendance Summary ===");
        LocalDate startDate = readDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = readDate("Enter end date (YYYY-MM-DD): ");

        Map<Integer, Double> summary = attendanceEngine.classAttendanceSummary(PRIMARY_FIVE, startDate, endDate);
        if (summary.isEmpty()) {
            System.out.println("No pupils found for " + PRIMARY_FIVE + ".");
            return;
        }

        summary.forEach((pupilId, percentage) ->
                System.out.printf(Locale.ROOT, "Pupil %d: %.2f%%%n", pupilId, percentage));
    }

    private void viewClassRankings() {
        System.out.println("\n=== Class Rankings and Reports ===");
        List<Subject> subjects = subjectDAO.findAll();
        if (subjects.isEmpty()) {
            System.out.println("No subjects are available.");
            return;
        }

        printSubjects(subjects);
        Subject selectedSubject = selectSubject(subjects);
        AcademicSession session = AcademicPeriodPrompts.promptForSession(scanner, "Enter academic session (YYYY/YYYY): ");
        AcademicTerm term = AcademicPeriodPrompts.promptForTerm(scanner, "Select a term by number or name: ");
        String sessionValue = session.toString();
        String termValue = term.getDisplayName();

        List<Score> scores = new ArrayList<>();
        for (Pupil pupil : pupilDAO.findByClassLevel(PRIMARY_FIVE)) {
            Score score = scoreDAO.findByPupilSubjectTerm(
                    pupil.getPupilId(),
                    selectedSubject.getSubjectId(),
                    sessionValue,
                    termValue
            );
            if (score != null) {
                scores.add(score);
            }
        }

        if (scores.isEmpty()) {
            System.out.println("No scores were found for " + selectedSubject.getName() + " in " + termValue + ", " + sessionValue + ".");
            return;
        }

        gradingEngine.rankClass(scores);

        System.out.println();
        System.out.println("Subject: " + selectedSubject.getName());
        System.out.println("Class: " + PRIMARY_FIVE);
        System.out.println("Session: " + sessionValue);
        System.out.println("Term: " + termValue);
        System.out.printf(Locale.ROOT, "%-12s %-12s %-12s %-12s%n", "Pupil ID", "Test", "Exam", "Rank");
        for (Score score : scores) {
            System.out.printf(
                    Locale.ROOT,
                    "%-12d %-12.1f %-12.1f %-12d%n",
                    score.getPupilId(),
                    score.getTestScore(),
                    score.getExamScore(),
                    score.getRank()
            );
        }
    }

    private void triggerDataSync() {
        System.out.println("\n=== Data Sync ===");
        String mode = readText("Export or import? (E/I): ").toUpperCase(Locale.ROOT);
        String path = readText("Enter the USB mount path: ");

        try {
            if (mode.startsWith("E")) {
                syncExporter.export(path);
            } else if (mode.startsWith("I")) {
                syncImporter.importFrom(path);
            } else {
                System.out.println("Unrecognized sync option. Use E for export or I for import.");
            }
        } catch (RuntimeException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }

    private void printSubjects(List<Subject> subjects) {
        System.out.println("Available subjects:");
        for (int index = 0; index < subjects.size(); index++) {
            Subject subject = subjects.get(index);
            System.out.printf(Locale.ROOT, "%d. %s%n", index + 1, subject.getName());
        }
    }

    private Subject selectSubject(List<Subject> subjects) {
        while (true) {
            int selection = readChoice("Select a subject by number: ");
            if (selection >= 1 && selection <= subjects.size()) {
                return subjects.get(selection - 1);
            }
            System.out.println("Invalid subject selection.");
        }
    }

    private int readChoice(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be blank.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readText(prompt));
            } catch (DateTimeParseException exception) {
                System.out.println("Please enter a valid date in YYYY-MM-DD format.");
            }
        }
    }
}
