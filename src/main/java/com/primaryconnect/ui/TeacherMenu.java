package com.primaryconnect.ui;

import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SubjectDAO;
import com.primaryconnect.data.TopicDAO;
import com.primaryconnect.model.Pupil;
import com.primaryconnect.model.Score;
import com.primaryconnect.model.Subject;
import com.primaryconnect.model.Topic;
import com.primaryconnect.service.AttendanceEngine;
import com.primaryconnect.service.GradingEngine;
import com.primaryconnect.service.MediaLauncher;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Provides the console menu for a teacher, enabling score entry, attendance marking, and media lesson launch actions.
 */
public class TeacherMenu {
    private static final String PRIMARY_FIVE = "Primary 5";

    private final Scanner scanner;
    private final AttendanceEngine attendanceEngine;
    private final GradingEngine gradingEngine;
    private final MediaLauncher mediaLauncher;
    private final PupilDAO pupilDAO;
    private final SubjectDAO subjectDAO;
    private final TopicDAO topicDAO;
    private final ScoreDAO scoreDAO;

    public TeacherMenu() {
        this.scanner = new Scanner(System.in);
        this.attendanceEngine = new AttendanceEngine();
        this.gradingEngine = new GradingEngine();
        this.mediaLauncher = new MediaLauncher();
        this.pupilDAO = new PupilDAO();
        this.subjectDAO = new SubjectDAO();
        this.topicDAO = new TopicDAO();
        this.scoreDAO = new ScoreDAO();
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
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private void markAttendance() {
        System.out.println("\n=== Mark Attendance ===");
        Pupil pupil = selectPupil();
        String status = readStatus("Enter status (PRESENT/ABSENT/LATE): ");
        LocalDate date = useTodayOrPromptDate();

        attendanceEngine.recordAttendance(pupil.getPupilId(), date, status);
        System.out.println("Attendance recorded for " + pupil.getName() + " on " + date + ".");
    }

    private void enterScores() {
        System.out.println("\n=== Enter Scores ===");
        Pupil pupil = selectPupil();
        Subject subject = selectSubject();
        String session = readText("Enter session: ");
        String term = readText("Enter term: ");
        double testScore = readDouble("Enter test score (0-40): ");
        double examScore = readDouble("Enter exam score (0-60): ");

        double finalScore = gradingEngine.computeTotal(testScore, examScore);
        String grade = gradingEngine.assignGrade(finalScore);

        Score existingScore = scoreDAO.findByPupilSubjectTerm(
                pupil.getPupilId(),
                subject.getSubjectId(),
                session,
                term
        );

        if (existingScore == null) {
            Score score = new Score(
                    0,
                    pupil.getPupilId(),
                    subject.getSubjectId(),
                    session,
                    term,
                    testScore,
                    examScore,
                    finalScore,
                    grade
            );
            scoreDAO.create(score);
            System.out.println("Score created for " + pupil.getName() + " in " + subject.getName() + ".");
        } else {
            existingScore.setTestScore(testScore);
            existingScore.setExamScore(examScore);
            existingScore.setFinalScore(finalScore);
            existingScore.setGrade(grade);
            scoreDAO.update(existingScore);
            System.out.println("Score updated for " + pupil.getName() + " in " + subject.getName() + ".");
        }
    }

    private void launchMediaLesson() {
        System.out.println("\n=== Launch Media Lesson ===");
        Subject subject = selectSubject();
        String term = readText("Enter term: ");
        List<Topic> topics = topicDAO.findBySubjectClassLevelTerm(subject.getSubjectId(), PRIMARY_FIVE, term);

        if (topics.isEmpty()) {
            System.out.println("No topics were found for " + subject.getName() + " in " + term + ".");
            return;
        }

        printTopics(topics);
        Topic topic = selectTopic(topics);
        if (topic.getMediaPath() == null || topic.getMediaPath().isBlank()) {
            System.out.println("No media path is configured for this topic.");
            return;
        }

        mediaLauncher.launch(topic.getMediaPath());
    }

    private void logout() {
        SessionContext.getInstance().logout();
        System.out.println("\nLogged out successfully.");
    }

    private Pupil selectPupil() {
        List<Pupil> pupils = pupilDAO.findByClassLevel(PRIMARY_FIVE);
        if (pupils.isEmpty()) {
            throw new IllegalStateException("No pupils are registered for " + PRIMARY_FIVE + ".");
        }

        printPupils(pupils);
        return pupils.get(readBoundedChoice("Select a pupil by number: ", pupils.size()) - 1);
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

    private void printPupils(List<Pupil> pupils) {
        System.out.println("Available pupils:");
        for (int index = 0; index < pupils.size(); index++) {
            Pupil pupil = pupils.get(index);
            System.out.printf(Locale.ROOT, "%d. %s (ID %d)%n", index + 1, pupil.getName(), pupil.getPupilId());
        }
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

    private String readStatus(String prompt) {
        while (true) {
            String status = readText(prompt).toUpperCase(Locale.ROOT);
            if ("PRESENT".equals(status) || "ABSENT".equals(status) || "LATE".equals(status)) {
                return status;
            }
            System.out.println("Status must be PRESENT, ABSENT, or LATE.");
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException exception) {
                System.out.println("Please enter a valid numeric value.");
            }
        }
    }

    private LocalDate useTodayOrPromptDate() {
        System.out.print("Use today's date? (Y/n): ");
        String response = scanner.nextLine().trim();
        if (response.isEmpty() || response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes")) {
            return LocalDate.now();
        }
        return readDate("Enter date (YYYY-MM-DD): ");
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
