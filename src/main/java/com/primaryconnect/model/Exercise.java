package com.primaryconnect.model;

/**
 * Represents an exercise item that can be presented to a pupil and auto-graded.
 */
public class Exercise {
    private int exerciseId;
    private int topicId;
    private String type;
    private String questionText;
    private String options;
    private String correctAnswer;

    public Exercise() {
    }

    public Exercise(
            int exerciseId,
            int topicId,
            String type,
            String questionText,
            String options,
            String correctAnswer
    ) {
        this.exerciseId = exerciseId;
        this.topicId = topicId;
        this.type = type;
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
