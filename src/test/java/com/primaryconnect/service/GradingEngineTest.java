package com.primaryconnect.service;

import com.primaryconnect.model.Score;
import java.util.ArrayList;
import java.util.List;

public class GradingEngineTest {

    public static void main(String[] args) {
        GradingEngine engine = new GradingEngine();

        // Test 1: computeTotal
        System.out.println("=== Test computeTotal ===");
        System.out.println("30 + 45 = " + engine.computeTotal(30, 45));
        System.out.println("25 + 40 = " + engine.computeTotal(25, 40));
        System.out.println("50 + 40 = " + engine.computeTotal(50, 40));
        System.out.println();

        // Test 2: assignGrade
        System.out.println("=== Test assignGrade ===");
        System.out.println("75 → " + engine.assignGrade(75));
        System.out.println("65 → " + engine.assignGrade(65));
        System.out.println("55 → " + engine.assignGrade(55));
        System.out.println("45 → " + engine.assignGrade(45));
        System.out.println("30 → " + engine.assignGrade(30));
        System.out.println();

        // Test 3: rankClass
        System.out.println("=== Test rankClass ===");
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.add(new Score(2, 102, 1, "2025/2026", "First", 25, 40, 65, "B"));
        scores.add(new Score(3, 103, 1, "2025/2026", "First", 20, 25, 45, "D"));
        scores.add(new Score(4, 104, 1, "2025/2026", "First", 30, 45, 75, "A"));

        engine.rankClass(scores);

        for (Score s : scores) {
            System.out.println("Pupil " + s.getPupilId() + " → Total: " + s.getFinalScore() + " Grade: " + s.getGrade() + " Rank: " + s.getRank());
        }
        System.out.println();

        // Test 4: generatePupilReport
        System.out.println(engine.generatePupilReport(101, scores, "First", "2025/2026"));

        // Test 5: generateClassReport
        System.out.println(engine.generateClassReport(scores, 5, "First", "2025/2026"));
    }
}