package com.primaryconnect.service;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.DatabaseManager;
import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Pupil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceEngineTest {
    private static final String SCHEMA_RESOURCE = "db/schema.sql";

    private Connection connection;
    private AttendanceEngine attendanceEngine;
    private AttendanceDAO attendanceDAO;
    private PupilDAO pupilDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        initializeSchema(connection);
        installConnection(connection);

        attendanceDAO = new AttendanceDAO();
        pupilDAO = new PupilDAO();
        attendanceEngine = new AttendanceEngine();
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseManager.getInstance().closeConnection();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void recordAttendanceCreatesNewRow() {
        int pupilId = createPupil("Ada", "Primary 5");
        LocalDate date = LocalDate.of(2026, 8, 10);

        attendanceEngine.recordAttendance(pupilId, date, "PRESENT");

        List<AttendanceRecord> records = attendanceDAO.findByPupilAndDateRange(pupilId, date, date);
        assertEquals(1, records.size());
        assertEquals(pupilId, records.get(0).getPupilId());
        assertEquals(date, records.get(0).getDate());
        assertEquals("PRESENT", records.get(0).getStatus());
    }

    @Test
    void recordAttendanceUpdatesExistingDay() {
        int pupilId = createPupil("Bola", "Primary 5");
        LocalDate date = LocalDate.of(2026, 8, 11);

        AttendanceRecord existingRecord = new AttendanceRecord(0, pupilId, date, "ABSENT");
        attendanceDAO.create(existingRecord);

        attendanceEngine.recordAttendance(pupilId, date, "LATE");

        List<AttendanceRecord> records = attendanceDAO.findByPupilAndDateRange(pupilId, date, date);
        assertEquals(1, records.size());
        assertEquals(existingRecord.getAttendanceId(), records.get(0).getAttendanceId());
        assertEquals("LATE", records.get(0).getStatus());
    }

    @Test
    void recordAttendanceRejectsInvalidStatus() {
        int pupilId = createPupil("Chidi", "Primary 5");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> attendanceEngine.recordAttendance(pupilId, LocalDate.of(2026, 8, 12), "PRESENTED")
        );

        assertTrue(exception.getMessage().contains("Invalid attendance status"));
        assertTrue(exception.getMessage().contains("PRESENT, ABSENT, or LATE"));
    }

    @Test
    void calculateAttendancePercentageCountsPresentAndLateAsAttended() {
        int pupilId = createPupil("Dami", "Primary 5");
        LocalDate start = LocalDate.of(2026, 8, 13);
        LocalDate middle = LocalDate.of(2026, 8, 14);
        LocalDate end = LocalDate.of(2026, 8, 15);

        attendanceDAO.create(new AttendanceRecord(0, pupilId, start, "PRESENT"));
        attendanceDAO.create(new AttendanceRecord(0, pupilId, middle, "ABSENT"));
        attendanceDAO.create(new AttendanceRecord(0, pupilId, end, "LATE"));

        double percentage = attendanceEngine.calculateAttendancePercentage(pupilId, start, end);

        assertEquals(66.66666666666667, percentage, 0.0001);
    }

    @Test
    void calculateAttendancePercentageReturnsZeroWhenNoRecordsExist() {
        int pupilId = createPupil("Efe", "Primary 5");

        double percentage = attendanceEngine.calculateAttendancePercentage(
                pupilId,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 31)
        );

        assertEquals(0.0, percentage);
    }

    @Test
    void classAttendanceSummaryReturnsPercentagesForEachPupilInTheClass() {
        int firstPupilId = createPupil("Fola", "Primary 5");
        int secondPupilId = createPupil("Goke", "Primary 5");
        createPupil("Hauwa", "Primary 4");
        LocalDate start = LocalDate.of(2026, 8, 16);
        LocalDate end = LocalDate.of(2026, 8, 18);

        attendanceDAO.create(new AttendanceRecord(0, firstPupilId, start, "PRESENT"));
        attendanceDAO.create(new AttendanceRecord(0, firstPupilId, start.plusDays(1), "LATE"));
        attendanceDAO.create(new AttendanceRecord(0, firstPupilId, end, "ABSENT"));
        attendanceDAO.create(new AttendanceRecord(0, secondPupilId, start, "ABSENT"));

        Map<Integer, Double> summary = attendanceEngine.classAttendanceSummary("Primary 5", start, end);

        assertEquals(2, summary.size());
        assertTrue(summary.containsKey(firstPupilId));
        assertTrue(summary.containsKey(secondPupilId));
        assertEquals(66.66666666666667, summary.get(firstPupilId), 0.0001);
        assertEquals(0.0, summary.get(secondPupilId));
    }

    private int createPupil(String name, String classLevel) {
        return pupilDAO.create(new Pupil(0, name, classLevel, "08000000000"));
    }

    private void installConnection(Connection newConnection) throws Exception {
        Field connectionField = DatabaseManager.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(DatabaseManager.getInstance(), newConnection);
    }

    private void initializeSchema(Connection activeConnection) throws IOException, SQLException {
        String schemaSql = readResource(SCHEMA_RESOURCE);

        try (Statement statement = activeConnection.createStatement()) {
            for (String sqlStatement : schemaSql.split(";")) {
                String trimmedStatement = sqlStatement.trim();
                if (!trimmedStatement.isEmpty()) {
                    statement.execute(trimmedStatement);
                }
            }
        }
    }

    private String readResource(String resourceName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing test resource: " + resourceName);
            }

            StringBuilder builder = new StringBuilder();
            try (
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                }
            }
            return builder.toString();
        }
    }
}
