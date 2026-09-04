package com.primaryconnect.ui;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SubjectDAO;
import com.primaryconnect.data.TopicDAO;
import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;
import com.primaryconnect.model.Pupil;
import com.primaryconnect.model.Score;
import com.primaryconnect.model.Subject;
import com.primaryconnect.model.Topic;
import com.primaryconnect.service.AttendanceEngine;
import com.primaryconnect.service.ExerciseEngine;
import com.primaryconnect.service.GradingEngine;
import com.primaryconnect.util.AcademicPeriodPrompts;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Provides the console menu for a pupil, allowing practice exercises and access to the learner's own report.
 */
public class PupilMenu {
    private static final String PRIMARY_FIVE = "Primary 5";

    private final Scanner scanner;
    private final ExerciseEngine exerciseEngine;
    private final AttendanceEngine attendanceEngine;
    private final GradingEngine gradingEngine;
    private final PupilDAO pupilDAO;
    private final SubjectDAO subjectDAO;
    private final TopicDAO topicDAO;
    private final ScoreDAO scoreDAO;

    public PupilMenu() {
        this.scanner = new Scanner(System.in);
        this.exerciseEngine = new ExerciseEngine();
        this.attendanceEngine = new AttendanceEngine();
        this.gradingEngine = new GradingEngine();
        this.pupilDAO = new PupilDAO();
        this.subjectDAO = new SubjectDAO();
        this.topicDAO = new TopicDAO();
        this.scoreDAO = new ScoreDAO();
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
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void takeExercise() {
        System.out.println("\n=== Take an Exercise ===");
        int pupilId = requirePupilId();
        if (pupilId < 0) {
            return;
        }

        Subject subject = selectSubject();
        AcademicTerm term = AcademicPeriodPrompts.promptForTerm(scanner, "Select a term by number or name: ");
        String termValue = term.getDisplayName();
        List<Topic> topics = topicDAO.findBySubjectClassLevelTerm(subject.getSubjectId(), PRIMARY_FIVE, termValue);

        if (topics.isEmpty()) {
            System.out.println("No topics were found for " + subject.getName() + " in " + termValue + ".");
            return;
        }

        printTopics(topics);
        Topic topic = selectTopic(topics);
        System.out.println("Starting exercise session for pupil " + pupilId + " on " + topic.getTitle() + ".");
        exerciseEngine.runExerciseSession(topic.getTopicId());
    }

    private void viewAttendanceRecord() {
        System.out.println("\n=== My Attendance Record ===");
        int pupilId = requirePupilId();
        if (pupilId < 0) {
            return;
        }

        LocalDate startDate = readDate("Enter start date (YYYY-MM-DD): ");
        LocalDate endDate = readDate("Enter end date (YYYY-MM-DD): ");
        double percentage = attendanceEngine.calculateAttendancePercentage(pupilId, startDate, endDate);

        System.out.printf(Locale.ROOT, "Attendance for pupil %d between %s and %s: %.2f%%%n",
                pupilId, startDate, endDate, percentage);
    }

    private void viewReportCard() {
        System.out.println("\n=== My Report Card ===");
        int pupilId = requirePupilId();
        if (pupilId < 0) {
            return;
        }

        AcademicSession session = AcademicPeriodPrompts.promptForSession(scanner, "Enter academic session (YYYY/YYYY): ");
        AcademicTerm term = AcademicPeriodPrompts.promptForTerm(scanner, "Select a term by number or name: ");
        String sessionValue = session.toString();
        String termValue = term.getDisplayName();
        List<Score> scores = scoreDAO.findAllByPupil(pupilId);
        List<Score> scoresForSelectedTerm = new ArrayList<>();

        for (Score score : scores) {
            if (termValue.equals(score.getTerm()) && sessionValue.equals(score.getSession())) {
                scoresForSelectedTerm.add(score);
            }
        }

        if (!scoresForSelectedTerm.isEmpty()) {
            gradingEngine.rankClass(scoresForSelectedTerm);
        }

        System.out.println(gradingEngine.generatePupilReport(pupilId, scores, termValue, sessionValue));
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }

    private int requirePupilId() {
        if (SessionContext.getInstance().getCurrentUser() == null
                || SessionContext.getInstance().getCurrentUser().getLinkedId() == null) {
            System.out.println("No pupil account is linked to the current session.");
            return -1;
        }
        return SessionContext.getInstance().getCurrentUser().getLinkedId();
    }

    private Subject selectSubject() {
        List<Subject> subjects = subjectDAO.findAll();
        if (subjects.isEmpty()) {
            throw new IllegalStateException("No subjects are available.");
        }

        printSubjects(subjects);
        return subjects.get(readBoundedChoice("Select a subject by number: ", subjects.size()) - 1);
    }

    private Topic selectTopic(List<Topic> topics) {
        return topics.get(readBoundedChoice("Select a topic by number: ", topics.size()) - 1);
    }

    private void printSubjects(List<Subject> subjects) {
        System.out.println("Available subjects:");
        for (int index = 0; index < subjects.size(); index++) {
            System.out.printf(Locale.ROOT, "%d. %s%n", index + 1, subjects.get(index).getName());
        }
    }

    private void printTopics(List<Topic> topics) {
        System.out.println("Available topics:");
        for (int index = 0; index < topics.size(); index++) {
            Topic topic = topics.get(index);
            System.out.printf(Locale.ROOT, "%d. %s%n", index + 1, topic.getTitle());
        }
    }

    private int readBoundedChoice(String prompt, int upperBound) {
        while (true) {
            int choice = readChoice(prompt);
            if (choice >= 1 && choice <= upperBound) {
                return choice;
            }
            System.out.println("Please select a number between 1 and " + upperBound + ".");
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
