package com.primaryconnect.service;

import com.primaryconnect.model.Score;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class GradingEngineTest {

    private final GradingEngine engine = new GradingEngine();

    @Test
    public void testComputeTotal() {
        assertEquals(75.0, engine.computeTotal(30, 45));
        assertEquals(65.0, engine.computeTotal(25, 40));
        assertEquals(100.0, engine.computeTotal(40, 60));
    }

    @Test
    public void testComputeTotalInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> engine.computeTotal(50, 40));
        assertThrows(IllegalArgumentException.class, () -> engine.computeTotal(30, 70));
        assertThrows(IllegalArgumentException.class, () -> engine.computeTotal(-5, 40));
    }

    @Test
    public void testAssignGrade() {
        assertEquals("A", engine.assignGrade(75));
        assertEquals("B", engine.assignGrade(65));
        assertEquals("C", engine.assignGrade(55));
        assertEquals("D", engine.assignGrade(45));
        assertEquals("E", engine.assignGrade(35));
        assertEquals("F", engine.assignGrade(20));
    }

    @Test
    public void testRankClass() {
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.add(new Score(2, 102, 1, "2025/2026", "First", 25, 40, 65, "B"));
        scores.add(new Score(3, 103, 1, "2025/2026", "First", 20, 25, 45, "D"));

        engine.rankClass(scores);

        assertEquals(1, scores.get(0).getRank());
        assertEquals(2, scores.get(1).getRank());
        assertEquals(3, scores.get(2).getRank());
    }

    @Test
    public void testRankClassWithTies() {
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.add(new Score(2, 104, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.add(new Score(3, 102, 1, "2025/2026", "First", 25, 40, 65, "B"));

        engine.rankClass(scores);

        assertEquals(1, scores.get(0).getRank());
        assertEquals(1, scores.get(1).getRank());
        assertEquals(3, scores.get(2).getRank());
    }

    @Test
    public void testGeneratePupilReport() {
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.get(0).setRank(1);

        String report = engine.generatePupilReport(101, scores, "First", "2025/2026");

        assertTrue(report.contains("Pupil ID: 101"));
        assertTrue(report.contains("First"));
        assertTrue(report.contains("2025/2026"));
    }

    @Test
    public void testGenerateClassReport() {
        List<Score> scores = new ArrayList<>();
        scores.add(new Score(1, 101, 1, "2025/2026", "First", 30, 45, 75, "A"));
        scores.add(new Score(2, 102, 1, "2025/2026", "First", 25, 40, 65, "B"));
        scores.get(0).setRank(1);
        scores.get(1).setRank(2);

        String report = engine.generateClassReport(scores, 5, "First", "2025/2026");

        assertTrue(report.contains("Primary 5"));
        assertTrue(report.contains("101"));
        assertTrue(report.contains("102"));
    }
}