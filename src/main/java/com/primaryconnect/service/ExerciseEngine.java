package com.primaryconnect.service;

import com.primaryconnect.data.ExerciseDAO;
import com.primaryconnect.model.Exercise;

import java.util.List;
import java.util.Scanner;

public class ExerciseEngine {

    private final ExerciseDAO exerciseDAO;
    private final Scanner scanner;

    public ExerciseEngine() {
        this.exerciseDAO = new ExerciseDAO();
        this.scanner = new Scanner(System.in);
    }

    public List<Exercise> loadExercises(int topicId) {
        return exerciseDAO.findByTopic(topicId);
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
}
