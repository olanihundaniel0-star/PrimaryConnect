package com.primaryconnect.service;

import com.primaryconnect.model.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles serving exercise questions to a pupil, checking their answers,
 * and giving instant feedback.
 *
 * Adapted to match the Exercise model as defined on feature/db-setup:
 * exerciseId, topicId, type, questionText, options (single delimited string), correctAnswer.
 *
 * Branch: feature/exercise-grading
 * Owner: Nkpogone Barile Michael
 */
public class ExerciseEngine {

    private final List<Exercise> hardcodedExercises;
    private final Scanner scanner;

    public ExerciseEngine() {
        this.hardcodedExercises = buildSampleData();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Returns a list of exercises filtered by topic.
     * (subjectId/classLevel filtering removed — those fields no longer
     * exist on the Exercise model from feature/db-setup.)
     */
    public List<Exercise> loadExercises(int topicId) {
        List<Exercise> results = new ArrayList<>();
        for (Exercise ex : hardcodedExercises) {
            if (ex.getTopicId() == topicId) {
                results.add(ex);
            }
        }
        return results;
    }

    public void presentQuestion(Exercise exercise) {
        System.out.println(exercise.getQuestionText());

        if ("MCQ".equals(exercise.getType())) {
            String[] options = exercise.getOptions().split(",");
            String[] labels = {"A", "B", "C", "D"};
            for (int i = 0; i < options.length && i < labels.length; i++) {
                System.out.println(labels[i] + ") " + options[i].trim());
            }
        } else if ("FILL_IN_BLANK".equals(exercise.getType())) {
            System.out.println("Fill in the blank: _______");
        }
    }

    public boolean checkAnswer(Exercise exercise, String userAnswer) {
        if (userAnswer == null || exercise.getCorrectAnswer() == null) {
            return false;
        }
        return userAnswer.trim().equalsIgnoreCase(exercise.getCorrectAnswer().trim());
    }

    public void giveFeedback(boolean isCorrect, Exercise exercise) {
        if (isCorrect) {
            System.out.println("Correct!");
        } else {
            System.out.println("Wrong! The answer was: " + exercise.getCorrectAnswer());
        }
    }

    public int runExerciseSession(int topicId) {
        List<Exercise> exercises = loadExercises(topicId);
        int correctCount = 0;

        if (exercises.isEmpty()) {
            System.out.println("No exercises available for this topic.");
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

    private List<Exercise> buildSampleData() {
        List<Exercise> data = new ArrayList<>();

        // MCQ sample — options stored as one comma-separated string
        data.add(new Exercise(
                1, 1,
                "MCQ",
                "What is 5 + 3?",
                "6,7,8,9",
                "C"
        ));

        // Fill-in-the-blank sample
        data.add(new Exercise(
                2, 2,
                "FILL_IN_BLANK",
                "The capital of Nigeria is _______.",
                null,
                "Abuja"
        ));

        data.add(new Exercise(
                3, 1,
                "MCQ",
                "What is 10 - 4?",
                "5,6,7,8",
                "B"
        ));

        return data;
    }
}