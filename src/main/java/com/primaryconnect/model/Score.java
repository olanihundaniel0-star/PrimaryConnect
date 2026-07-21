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

    public Score(int scoreId, int pupilId, int subjectId, double testScore, double examScore, String term, String session) {
        this.scoreId = scoreId;
        this.pupilId = pupilId;
        this.subjectId = subjectId;
        this.testScore = testScore;
        this.examScore = examScore;
        this.totalScore = testScore + examScore;
        this.term = term;
        this.session = session;
    }
    public int getScoreId() { return scoreId; }
    public int getPupilId() { return pupilId; }
    public int getSubjectId() { return subjectId; }
    public double getTestScore() { return testScore; }
    public double getExamScore() { return examScore; }
    public double getTotalScore() { return totalScore; }
    public String getGrade() { return grade; }
    public String getTerm() { return term; }
    public String getSession() { return session; }
    public int getRank() { return rank; }

    public void setGrade(String grade) { this.grade = grade; }
    public void setRank(int rank) { this.rank = rank; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore;}
}