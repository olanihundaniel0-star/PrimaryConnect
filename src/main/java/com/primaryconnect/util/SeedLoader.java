package com.primaryconnect.util;

import com.primaryconnect.data.AttendanceDAO;
import com.primaryconnect.data.ExerciseDAO;
import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.data.SubjectDAO;
import com.primaryconnect.data.TopicDAO;
import com.primaryconnect.model.AttendanceRecord;
import com.primaryconnect.model.Exercise;
import com.primaryconnect.model.Pupil;
import com.primaryconnect.model.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads bundled CSV seed data into the database.
 */
public final class SeedLoader {
    private static final Logger LOGGER = Logger.getLogger(SeedLoader.class.getName());

    private static final String PUPILS_SEED = "seed/pupils_seed.csv";
    private static final String CURRICULUM_SEED = "seed/curriculum_seed.csv";
    private static final String EXERCISES_SEED = "seed/exercises_seed.csv";
    private static final String SAMPLE_RECORDS_SEED = "seed/sample_records_seed.csv";

    private SeedLoader() {
    }

    public static void loadAll() {
        // Most seed files are currently header-only placeholders waiting on content team data,
        // so an empty or near-empty load result right now is expected.
        PupilDAO pupilDAO = new PupilDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        TopicDAO topicDAO = new TopicDAO();
        ExerciseDAO exerciseDAO = new ExerciseDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();
        Map<String, TopicLookup> loadedTopicsByTitle = new HashMap<>();

        loadPupils(pupilDAO);
        loadCurriculum(subjectDAO, topicDAO, loadedTopicsByTitle);
        loadExercises(topicDAO, exerciseDAO, loadedTopicsByTitle);
        loadAttendance(pupilDAO, attendanceDAO);
    }

    private static void loadPupils(PupilDAO pupilDAO) {
        readSeedRows(PUPILS_SEED, row -> {
            if (row.length < 3) {
                warnSkippingRow(PUPILS_SEED, row, "expected columns: name,class_level,guardian_contact");
                return;
            }

            pupilDAO.create(new Pupil(0, row[0], row[1], row[2]));
        });
    }

    private static void loadCurriculum(
            SubjectDAO subjectDAO,
            TopicDAO topicDAO,
            Map<String, TopicLookup> loadedTopicsByTitle
    ) {
        readSeedRows(CURRICULUM_SEED, row -> {
            if (row.length < 4) {
                warnSkippingRow(CURRICULUM_SEED, row, "expected columns: subject,class_level,topic,media_path");
                return;
            }

            int subjectId = subjectDAO.findOrCreateByName(row[0]);
            Topic topic = new Topic(0, subjectId, row[1], row[2], row[3]);
            topicDAO.create(topic);
            loadedTopicsByTitle.put(row[2], new TopicLookup(subjectId, row[1]));
        });
    }

    private static void loadExercises(
            TopicDAO topicDAO,
            ExerciseDAO exerciseDAO,
            Map<String, TopicLookup> loadedTopicsByTitle
    ) {
        readSeedRows(EXERCISES_SEED, row -> {
            if (row.length < 5) {
                warnSkippingRow(EXERCISES_SEED, row, "expected columns: topic,type,question_text,options,correct_answer");
                return;
            }

            String title = row[0];
            Topic topic = findExerciseTopic(topicDAO, title, loadedTopicsByTitle.get(title));
            if (topic == null) {
                LOGGER.warning("Skipping exercise seed row because topic title was not found: " + title + ".");
                return;
            }

            exerciseDAO.create(new Exercise(0, topic.getTopicId(), row[1], row[2], row[3], row[4]));
        });
    }

    private static void loadAttendance(PupilDAO pupilDAO, AttendanceDAO attendanceDAO) {
        readSeedRows(SAMPLE_RECORDS_SEED, row -> {
            if (row.length < 3) {
                warnSkippingRow(SAMPLE_RECORDS_SEED, row, "expected columns: pupil_id,date,status");
                return;
            }

            int pupilId;
            try {
                pupilId = Integer.parseInt(row[0]);
            } catch (NumberFormatException exception) {
                warnSkippingRow(SAMPLE_RECORDS_SEED, row, "pupil_id is not a valid integer");
                return;
            }

            if (pupilDAO.findById(pupilId) == null) {
                LOGGER.warning("Skipping attendance seed row because pupil ID was not found: " + pupilId + ".");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(row[1]);
            } catch (DateTimeParseException exception) {
                warnSkippingRow(SAMPLE_RECORDS_SEED, row, "date is not a valid ISO-8601 date");
                return;
            }

            attendanceDAO.create(new AttendanceRecord(0, pupilId, date, row[2]));
        });
    }

    private static Topic findExerciseTopic(TopicDAO topicDAO, String title, TopicLookup topicLookup) {
        if (topicLookup != null) {
            Topic topic = topicDAO.findBySubjectClassLevelTitle(topicLookup.subjectId(), topicLookup.classLevel(), title);
            if (topic != null) {
                return topic;
            }
        }

        return topicDAO.findByTitle(title);
    }

    private static void readSeedRows(String resourceName, SeedRowConsumer rowConsumer) {
        ClassLoader classLoader = SeedLoader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                LOGGER.warning("Seed file was not found: " + resourceName + ". Continuing with next seed file.");
                return;
            }

            try (
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                boolean headerSkipped = false;
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!headerSkipped) {
                        headerSkipped = true;
                        continue;
                    }

                    if (line.isBlank()) {
                        continue;
                    }

                    rowConsumer.accept(splitCsvLine(line));
                }
            }
        } catch (IOException exception) {
            LOGGER.warning("Failed to read seed file " + resourceName + ": " + exception.getMessage() + ". Continuing with next seed file.");
        }
    }

    private static String[] splitCsvLine(String line) {
        String[] values = line.split(",", -1);
        for (int index = 0; index < values.length; index++) {
            values[index] = values[index].trim();
        }
        return values;
    }

    private static void warnSkippingRow(String resourceName, String[] row, String reason) {
        LOGGER.warning("Skipping seed row in " + resourceName + " because " + reason + ": " + String.join(",", row) + ".");
    }

    @FunctionalInterface
    private interface SeedRowConsumer {
        void accept(String[] row);
    }

    private record TopicLookup(int subjectId, String classLevel) {
    }
}
