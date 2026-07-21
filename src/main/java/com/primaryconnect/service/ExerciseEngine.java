package com.primaryconnect.service;

import com.primaryconnect.model.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles serving exercise questions to a pupil, checking their answers,
 * and giving instant feedback.
 *
 * Until feature/db-setup lands, this class uses a hardcoded ArrayList<Exercise>
 * as its data source. When the database is ready, replace loadExercises()'s
 * internal data with a call to ExerciseDAO — the method signature stays the same.
 *
 * Branch: feature/exercise-grading
 * Owner: Nkpogone Barile Michael
 */
public class ExerciseEngine {

    // Temporary hardcoded data source (stand-in for ExerciseDAO until db-setup lands)
    private final List<Exercise> hardcodedExercises;

    private final Scanner scanner;

    public ExerciseEngine() {
        this.hardcodedExercises = buildSampleData();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Returns a list of exercises filtered by subject and class level.
     * For now, filters the hardcoded sample data.
     *
     * @param subjectId  the subject to filter by
     * @param classLevel the Primary class level to filter by
     * @return matching exercises
     */
    public List<Exercise> loadExercises(int subjectId, int classLevel) {
        List<Exercise> results = new ArrayList<>();
        for (Exercise ex : hardcodedExercises) {
            if (ex.getSubjectId() == subjectId && ex.getClassLevel() == classLevel) {
                results.add(ex);
            }
        }
        return results;
    }

    /**
     * Prints the question to the console. Shows A/B/C/D options for MCQ,
     * or a blank prompt for fill-in-the-blank.
     *
     * @param exercise the exercise to present
     */
    public void presentQuestion(Exercise exercise) {
        System.out.println(exercise.getQuestionText());

        if ("MCQ".equals(exercise.getQuestionType())) {
            System.out.println("A) " + exercise.getOptionA());
            System.out.println("B) " + exercise.getOptionB());
            System.out.println("C) " + exercise.getOptionC());
            System.out.println("D) " + exercise.getOptionD());
        } else if ("FILL_IN_BLANK".equals(exercise.getQuestionType())) {
            System.out.println("Fill in the blank: _______");
        }
    }

    /**
     * Compares the pupil's submitted answer to the correct answer.
     * Comparison is case-insensitive and trims whitespace.
     *
     * @param exercise   the exercise being answered
     * @param userAnswer the pupil's submitted answer
     * @return true if correct, false otherwise
     */
    public boolean checkAnswer(Exercise exercise, String userAnswer) {
        if (userAnswer == null || exercise.getCorrectAnswer() == null) {
            return false;
        }
        return userAnswer.trim().equalsIgnoreCase(exercise.getCorrectAnswer().trim());
    }

    /**
     * Prints feedback to the console based on whether the answer was correct.
     *
     * @param isCorrect whether the pupil's answer was correct
     * @param exercise  the exercise that was answered
     */
    public void giveFeedback(boolean isCorrect, Exercise exercise) {
        if (isCorrect) {
            System.out.println("Correct!");
        } else {
            System.out.println("Wrong! The answer was: " + exercise.getCorrectAnswer());
        }
    }

    /**
     * Runs the full exercise flow: loads exercises for the given subject and
     * class level, then loops through each one presenting the question,
     * reading the pupil's answer, checking it, giving feedback, and tracking
     * the running score.
     *
     * @param subjectId  the subject to run the session for
     * @param classLevel the Primary class level to run the session for
     * @return the number of questions answered correctly
     */
    public int runExerciseSession(int subjectId, int classLevel) {
        List<Exercise> exercises = loadExercises(subjectId, classLevel);
        int correctCount = 0;

        if (exercises.isEmpty()) {
            System.out.println("No exercises available for this subject and class level.");
            return 0;
        }

        for (Exercise exercise : exercises) {
            presentQuestion(exercise);
            System.out.print("Your answer: ");
            String userAnswer = scanner.nextLine();

            boolean isCorrect = checkAnswer(exercise, userAnswer);
            giveFeedback(isCorrect, exercise);

            if (isCorrect) {
                correctCount++;
            }
            System.out.println();
        }

        System.out.println("Session complete: " + correctCount + "/" + exercises.size() + " correct.");
        return correctCount;
    }

    /**
     * Builds the hardcoded sample data used for testing until db-setup lands.
     * Includes both MCQ and fill-in-the-blank examples, per the module plan.
     */
    private List<Exercise> buildSampleData() {
        List<Exercise> data = new ArrayList<>();

        // Sample MCQ (subjectId 1 = Mathematics, classLevel 5)
        data.add(new Exercise(
                1, 1, 1, 5,
                "What is 5 + 3?",
                "MCQ",
                "6", "7", "8", "9",
                "C"
        ));

        // Sample Fill-in-the-blank (subjectId 2 = Basic Science, classLevel 5)
        data.add(new Exercise(
                2, 2, 2, 5,
                "The capital of Nigeria is _______.",
                "FILL_IN_BLANK",
                null, null, null, null,
                "Abuja"
        ));

        // A couple of extra samples for a fuller test session
        data.add(new Exercise(
                3, 1, 1, 5,
                "What is 10 - 4?",
                "MCQ",
                "5", "6", "7", "8",
                "B"
        ));

        data.add(new Exercise(
                4, 3, 3, 5,
                "The English word for a place where books are kept is a _______.",
                "FILL_IN_BLANK",
                null, null, null, null,
                "library"
        ));

        return data;
    }
}
