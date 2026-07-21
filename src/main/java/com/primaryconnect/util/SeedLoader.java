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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Loads bundled CSV seed data into the database.
 */
public final class SeedLoader {
    private static final Logger LOGGER = Logger.getLogger(SeedLoader.class.getName());

    private static final String PUPILS_SEED = "seed/pupils_seed.csv";
    private static final String CURRICULUM_SEED_DIRECTORY = "seed/curriculum";
    private static final String EXERCISES_SEED = "seed/exercises_seed.csv";
    private static final String SAMPLE_RECORDS_SEED = "seed/sample_records_seed.csv";
    private static final List<String> CURRICULUM_HEADERS = List.of(
            "class",
            "subject",
            "term",
            "week",
            "topic",
            "learningObjectives",
            "contents",
            "teacherActivities",
            "learnerActivities",
            "teachingMaterials",
            "assessment"
    );

    private SeedLoader() {
    }

    public static void loadAll() {
        // Some non-curriculum seed files are header-only placeholders waiting on content team data.
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
        for (String resourceName : findCurriculumSeedResources()) {
            loadCurriculumFile(resourceName, subjectDAO, topicDAO, loadedTopicsByTitle);
        }
    }

    private static void loadCurriculumFile(
            String resourceName,
            SubjectDAO subjectDAO,
            TopicDAO topicDAO,
            Map<String, TopicLookup> loadedTopicsByTitle
    ) {
        ClassLoader classLoader = SeedLoader.class.getClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                LOGGER.warning("Curriculum seed file was not found: " + resourceName + ". Continuing with next seed file.");
                return;
            }

            String csvContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            if (csvContent.startsWith("\uFEFF")) {
                csvContent = csvContent.substring(1);
            }

            try (
                    StringReader reader = new StringReader(csvContent);
                    CSVParser parser = CSVFormat.DEFAULT.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreEmptyLines(true)
                            .setTrim(true)
                            .get()
                            .parse(reader)
            ) {
                if (!hasExpectedCurriculumHeaders(parser)) {
                    LOGGER.warning("Skipping curriculum seed file " + resourceName
                            + " because it does not contain the expected headers: "
                            + String.join(",", CURRICULUM_HEADERS) + ".");
                    return;
                }

                for (CSVRecord record : parser) {
                    loadCurriculumRecord(resourceName, record, subjectDAO, topicDAO, loadedTopicsByTitle);
                }
            }
        } catch (IOException exception) {
            LOGGER.warning("Failed to read curriculum seed file " + resourceName + ": "
                    + exception.getMessage() + ". Continuing with next seed file.");
        }
    }

    private static void loadCurriculumRecord(
            String resourceName,
            CSVRecord record,
            SubjectDAO subjectDAO,
            TopicDAO topicDAO,
            Map<String, TopicLookup> loadedTopicsByTitle
    ) {
        String classLevel = record.get("class");
        String subject = record.get("subject");
        String term = record.get("term");
        String title = record.get("topic");

        if (classLevel.isBlank() || subject.isBlank() || term.isBlank() || title.isBlank()) {
            warnSkippingCurriculumRecord(resourceName, record, "class, subject, term, and topic are required");
            return;
        }

        Integer week = parseOptionalWeek(resourceName, record);
        if (week == null && !record.get("week").isBlank()) {
            return;
        }

        int subjectId = subjectDAO.findOrCreateByName(subject);
        Topic existingTopic = topicDAO.findBySubjectClassLevelTermTitle(subjectId, classLevel, term, title);
        if (existingTopic != null) {
            loadedTopicsByTitle.put(title, new TopicLookup(subjectId, classLevel, term));
            return;
        }

        Topic topic = new Topic(
                0,
                subjectId,
                classLevel,
                title,
                term,
                week,
                record.get("learningObjectives"),
                record.get("contents"),
                record.get("teacherActivities"),
                record.get("learnerActivities"),
                record.get("teachingMaterials"),
                record.get("assessment"),
                null
        );
        topicDAO.create(topic);
        loadedTopicsByTitle.put(title, new TopicLookup(subjectId, classLevel, term));
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
            Topic topic = topicDAO.findBySubjectClassLevelTermTitle(
                    topicLookup.subjectId(),
                    topicLookup.classLevel(),
                    topicLookup.term(),
                    title
            );
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

    private static List<String> findCurriculumSeedResources() {
        ClassLoader classLoader = SeedLoader.class.getClassLoader();
        URL directoryUrl = classLoader.getResource(CURRICULUM_SEED_DIRECTORY);

        if (directoryUrl == null) {
            LOGGER.warning("Curriculum seed directory was not found: " + CURRICULUM_SEED_DIRECTORY + ". Continuing without curriculum seeds.");
            return List.of();
        }

        try {
            if ("jar".equals(directoryUrl.getProtocol())) {
                return findCurriculumSeedResourcesInJar(directoryUrl);
            }

            Path directoryPath = Path.of(directoryUrl.toURI());
            try (Stream<Path> stream = Files.list(directoryPath)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(SeedLoader::isCsvFile)
                        .sorted()
                        .map(fileName -> CURRICULUM_SEED_DIRECTORY + "/" + fileName)
                        .toList();
            }
        } catch (IOException | URISyntaxException exception) {
            LOGGER.warning("Failed to scan curriculum seed directory " + CURRICULUM_SEED_DIRECTORY + ": "
                    + exception.getMessage() + ". Continuing without curriculum seeds.");
            return List.of();
        }
    }

    private static List<String> findCurriculumSeedResourcesInJar(URL directoryUrl) throws IOException {
        JarURLConnection jarConnection = (JarURLConnection) directoryUrl.openConnection();
        List<String> resourceNames = new ArrayList<>();

        jarConnection.getJarFile().stream()
                .filter(entry -> !entry.isDirectory())
                .map(entry -> entry.getName())
                .filter(name -> name.startsWith(CURRICULUM_SEED_DIRECTORY + "/"))
                .filter(SeedLoader::isCsvFile)
                .sorted()
                .forEach(resourceNames::add);

        return resourceNames;
    }

    private static boolean isCsvFile(String fileName) {
        return fileName.toLowerCase().endsWith(".csv");
    }

    private static boolean hasExpectedCurriculumHeaders(CSVParser parser) {
        return parser.getHeaderNames().equals(CURRICULUM_HEADERS);
    }

    private static Integer parseOptionalWeek(String resourceName, CSVRecord record) {
        String value = record.get("week");
        if (value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            warnSkippingCurriculumRecord(resourceName, record, "week is not a valid integer");
            return null;
        }
    }

    private static void warnSkippingCurriculumRecord(String resourceName, CSVRecord record, String reason) {
        LOGGER.warning("Skipping curriculum seed row " + record.getRecordNumber() + " in "
                + resourceName + " because " + reason + ".");
    }

    @FunctionalInterface
    private interface SeedRowConsumer {
        void accept(String[] row);
    }

    private record TopicLookup(int subjectId, String classLevel, String term) {
    }
}
