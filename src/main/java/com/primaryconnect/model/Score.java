package com.primaryconnect.model;

public class Score {
    private int scoreId;
    private int pupilId;
    private int subjectId;
    private String session;
    private String term;
    private double testScore;
    private double examScore;
    private double finalScore;
    private String grade;
    private int rank;

    public Score() {
    }

    public Score(
            int scoreId,
            int pupilId,
            int subjectId,
            String session,
            String term,
            double testScore,
            double examScore,
            double finalScore,
            String grade
    ) {
        this.scoreId = scoreId;
        this.pupilId = pupilId;
        this.subjectId = subjectId;
        this.session = session;
        this.term = term;
        this.testScore = testScore;
        this.examScore = examScore;
        this.finalScore = finalScore;
        this.grade = grade;
    }

    public int getScoreId() { return scoreId; }
    public void setScoreId(int scoreId) { this.scoreId = scoreId; }
    public int getPupilId() { return pupilId; }
    public void setPupilId(int pupilId) { this.pupilId = pupilId; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public double getTestScore() { return testScore; }
    public void setTestScore(double testScore) { this.testScore = testScore; }
    public double getExamScore() { return examScore; }
    public void setExamScore(double examScore) { this.examScore = examScore; }
    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
}