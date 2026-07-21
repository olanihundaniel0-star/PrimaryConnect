package com.primaryconnect.service;

import com.primaryconnect.model.Score;
import java.util.ArrayList;
import java.util.List;

public class GradingEngine {

    public double computeTotal(double testScore, double examScore) {
        if (testScore < 0 || testScore > 40) {
            throw new IllegalArgumentException("Invalid test score. Must be between 0 and 40.");
        }
        if (examScore < 0 || examScore > 60) {
            throw new IllegalArgumentException("Invalid exam score. Must be between 0 and 60.");
        }
        return testScore + examScore;
    }

    public String assignGrade(double totalScore) {
        if (totalScore >= 70) return "A";
        if (totalScore >= 60) return "B";
        if (totalScore >= 50) return "C";
        if (totalScore >= 40) return "D";
        if (totalScore >= 30) return "E";
        return "F";
    }

    public void rankClass(List<Score> scores) {
        scores.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));

        for (int i = 0; i < scores.size(); i++) {
            if (i > 0 && scores.get(i).getFinalScore() == scores.get(i - 1).getFinalScore()) {
                scores.get(i).setRank(scores.get(i - 1).getRank());
            } else {
                scores.get(i).setRank(i + 1);
            }
        }
    }

    public String generatePupilReport(int pupilId, List<Score> allScores, String term, String session) {
        StringBuilder report = new StringBuilder();
        report.append("=== End of Term Report ===\n");
        report.append("Pupil ID: ").append(pupilId).append("\n");
        report.append("Term: ").append(term).append("\n");
        report.append("Session: ").append(session).append("\n");
        report.append("---------------------------\n");
        report.append(String.format("%-10s %-6s %-6s %-6s %-6s %-6s\n", "Subject", "Test", "Exam", "Total", "Grade", "Rank"));

        for (Score s : allScores) {
            if (s.getPupilId() == pupilId && s.getTerm().equals(term) && s.getSession().equals(session)) {
                report.append(String.format("%-10d %-6.1f %-6.1f %-6.1f %-6s %-6d\n",
                    s.getSubjectId(), s.getTestScore(), s.getExamScore(),
                    s.getFinalScore(), s.getGrade(), s.getRank()));
            }
        }

        return report.toString();
    }

    public String generateClassReport(List<Score> scores, int classLevel, String term, String session) {
        StringBuilder report = new StringBuilder();
        report.append("=== Class Report ===\n");
        report.append("Class: Primary ").append(classLevel).append("\n");
        report.append("Term: ").append(term).append("\n");
        report.append("Session: ").append(session).append("\n");
        report.append("--------------------------\n");
        report.append(String.format("%-10s %-10s %-6s %-6s %-6s\n", "PupilID", "Subject", "Total", "Grade", "Rank"));

        for (Score s : scores) {
            if (s.getTerm().equals(term) && s.getSession().equals(session)) {
                report.append(String.format("%-10d %-10d %-6.1f %-6s %-6d\n",
                    s.getPupilId(), s.getSubjectId(),
                    s.getFinalScore(), s.getGrade(), s.getRank()));
            }
        }

        return report.toString();
    }
}