package com.primaryconnect.model;

public class Score {
    private int scoreId;
    private int pupilId;
    private int subjectId;
    private double testScore;
    private double examScore;
    private double totalScore;
    private String grade;
    private String term;
    private String session;
    private int rank;

    public Score(int scoreId, int pupilId, int subjectId, double testScore, double examScore, double totalScore, String term, String session) {
        this.scoreId = scoreId;
        this.pupilId = pupilId;
        this.subjectId = subjectId;
        this.testScore = testScore;
        this.examScore = examScore;
        this.totalScore = testScore + examScore;
        this.term = term;
        this.session = session;
    }
}