package com.primaryconnect.service;

import com.primaryconnect.model.Score;
import java.util.ArrayList;
import java.util.List;

public class GradingEngineTest {

    public static void main(String[] args) {
        GradingEngine engine = new GradingEngine();

        // Test 1: computeTotal
        System.out.println("=== Test computeTotal ===");
        System.out.println("30 + 45 = " + engine.computeTotal(30, 45));  // 75.0
        System.out.println("25 + 40 = " + engine.computeTotal(25, 40));  // 65.0
        System.out.println("50 + 40 = " + engine.computeTotal(50, 40));  // -1 (invalid test)
        System.out.println();

        // Test 2: assignGrade
        System.out.println("=== Test assignGrade ===");
        System.out.println("75 → " + engine.assignGrade(75));  // A
        System.out.println("65 → " + engine.assignGrade(65));  // B
        System.out.println("55 → " + engine.assignGrade(55));  // C
        System.out.println("45 → " + engine.assignGrade(45));  // D
        System.out.println("30 → " + engine.assignGrade(30));  // F
        System.out.println();

        // Test 3: rankClass
        System.out.println("=== Test rankClass ===");
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, 30, 45, "First", "2025/2026"));
        scores.add(new Score(2, 102, 1, 25, 40, "First", "2025/2026"));
        scores.add(new Score(3, 103, 1, 20, 25, "First", "2025/2026"));
        scores.add(new Score(4, 104, 1, 30, 45, "First", "2025/2026"));

        for (Score s : scores) {
            s.setGrade(engine.assignGrade(s.getTotalScore()));
        }
        engine.rankClass(scores);

        for (Score s : scores) {
            System.out.println("Pupil " + s.getPupilId() + " → Total: " + s.getTotalScore() + " Grade: " + s.getGrade() + " Rank: " + s.getRank());
        }
        System.out.println();

        // Test 4: generatePupilReport
        System.out.println(engine.generatePupilReport(101, scores, "First", "2025/2026"));

        // Test 5: generateClassReport
        System.out.println(engine.generateClassReport(scores, 5, "First", "2025/2026"));
    }
}