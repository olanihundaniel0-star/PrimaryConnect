package com.primaryconnect.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcademicPeriodTest {
    @Test
    void parsesAcademicTermLabelsAndChoices() {
        assertEquals(AcademicTerm.FIRST, AcademicTerm.fromLabel("First Term"));
        assertEquals(AcademicTerm.SECOND, AcademicTerm.fromLabel("second"));
        assertEquals(AcademicTerm.THIRD, AcademicTerm.fromChoice(3));
    }

    @Test
    void rejectsInvalidAcademicTermLabels() {
        assertThrows(IllegalArgumentException.class, () -> AcademicTerm.fromLabel("Holiday"));
        assertThrows(IllegalArgumentException.class, () -> AcademicTerm.fromChoice(4));
    }

    @Test
    void parsesAcademicSessions() {
        AcademicSession session = AcademicSession.parse("2026/2027");

        assertEquals(2026, session.startYear());
        assertEquals(2027, session.endYear());
        assertEquals("2026/2027", session.toString());
    }

    @Test
    void rejectsInvalidAcademicSessions() {
        assertThrows(IllegalArgumentException.class, () -> AcademicSession.parse("2026-2027"));
        assertThrows(IllegalArgumentException.class, () -> AcademicSession.parse("2026/2028"));
    }
}
