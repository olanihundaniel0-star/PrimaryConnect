package com.primaryconnect.ui;

import com.primaryconnect.data.DatabaseManager;
import com.primaryconnect.data.PupilDAO;
import com.primaryconnect.data.ScoreDAO;
import com.primaryconnect.data.SubjectDAO;
import com.primaryconnect.data.TopicDAO;
import com.primaryconnect.model.DashboardStats;
import com.primaryconnect.model.Pupil;
import com.primaryconnect.model.Score;
import com.primaryconnect.model.Subject;
import com.primaryconnect.model.Topic;
import com.primaryconnect.model.DashboardStats.MediaTopicPreview;
import com.primaryconnect.model.AcademicSession;
import com.primaryconnect.model.AcademicTerm;
import com.primaryconnect.service.AttendanceEngine;
import com.primaryconnect.service.DashboardService;
import com.primaryconnect.service.GradingEngine;
import com.primaryconnect.service.MediaLauncher;
import com.primaryconnect.service.SyncExporter;
import com.primaryconnect.service.SyncImporter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.time.LocalDate;
import java.time.Year;

/**
 * JavaFX dashboard styled from the HTML mockup in the repository root.
 */
public class DashboardApp extends Application {
    private static final double DASHBOARD_WIDTH = 1180.0;
    private static final double CARD_WIDTH = 245.0;
    private static final String PRIMARY_FIVE = "Primary 5";

    private final DashboardService dashboardService = new DashboardService();
    private final MediaLauncher mediaLauncher = new MediaLauncher();
    private final AttendanceEngine attendanceEngine = new AttendanceEngine();
    private final GradingEngine gradingEngine = new GradingEngine();
    private final PupilDAO pupilDAO = new PupilDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TopicDAO topicDAO = new TopicDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final SyncExporter syncExporter = new SyncExporter();
    private final SyncImporter syncImporter = new SyncImporter();

    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        DashboardStats stats = dashboardService.loadSnapshot();

        StackPane root = new StackPane();
        root.getStyleClass().add("dashboard-root");

        Node backdrop = buildBackdrop();
        VBox content = new VBox(22);
        content.setMaxWidth(DASHBOARD_WIDTH);
        content.setPadding(new Insets(24));

        content.getChildren().addAll(
                buildTopBar(stats),
                buildWelcomeBlock(stats),
                buildStatsGrid(stats),
                buildActionGrid(stats),
                buildLowerSection(stats)
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("dashboard-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setPadding(new Insets(0));
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        root.getChildren().addAll(backdrop, scrollPane);

        Scene scene = new Scene(root, 1280, 900);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/com/primaryconnect/ui/dashboard.css")
        ).toExternalForm());

        stage.setTitle("PrimaryConnect Dashboard");
        stage.setMinWidth(1040);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
    }

    private Node buildBackdrop() {
        Pane backdrop = new Pane();
        backdrop.setMouseTransparent(true);

        Circle coralBlob = new Circle(220, Color.web("#E8604F", 0.12));
        coralBlob.setTranslateX(470);
        coralBlob.setTranslateY(-250);

        Circle tealBlob = new Circle(170, Color.web("#2FB6A6", 0.12));
        tealBlob.setTranslateX(-520);
        tealBlob.setTranslateY(360);

        Circle goldBlob = new Circle(120, Color.web("#F2A93D", 0.10));
        goldBlob.setTranslateX(420);
        goldBlob.setTranslateY(300);

        backdrop.getChildren().addAll(coralBlob, tealBlob, goldBlob);
        return backdrop;
    }

    private HBox buildTopBar(DashboardStats stats) {
        Label brand = new Label("PrimaryConnect");
        brand.getStyleClass().add("dashboard-brand");

        Label role = new Label(stats.roleLabel());
        role.getStyleClass().add("role-chip");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, brand, spacer, role);
        topBar.getStyleClass().add("dashboard-top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        return topBar;
    }

    private VBox buildWelcomeBlock(DashboardStats stats) {
        Label kicker = new Label("Welcome back");
        kicker.getStyleClass().add("welcome-kicker");

        Label name = new Label(stats.displayName());
        name.getStyleClass().add("welcome-name");

        Label subtitle = new Label("A clean overview of pupils, media, attendance, and classroom activity.");
        subtitle.getStyleClass().add("welcome-subtitle");

        VBox welcome = new VBox(4, kicker, name, subtitle);
        return welcome;
    }

    private FlowPane buildStatsGrid(DashboardStats stats) {
        FlowPane statsGrid = new FlowPane();
        statsGrid.setHgap(12);
        statsGrid.setVgap(12);
        statsGrid.setPrefWrapLength(DASHBOARD_WIDTH);

        statsGrid.getChildren().addAll(
                buildStatCard("Pupils", Integer.toString(stats.pupilCount()), "Students registered", "#E8604F"),
                buildStatCard("Topics ready", Integer.toString(stats.mediaTopicCount()), "Topics with media", "#2FB6A6"),
                buildStatCard("Avg attendance", formatPercentage(stats.attendanceRate()), "Present or late", "#F2A93D")
        );

        return statsGrid;
    }

    private FlowPane buildActionGrid(DashboardStats stats) {
        FlowPane actionGrid = new FlowPane();
        actionGrid.setHgap(14);
        actionGrid.setVgap(14);
        actionGrid.setPrefWrapLength(DASHBOARD_WIDTH);

        actionGrid.getChildren().addAll(
                buildActionCard(
                        "A",
                        "Mark attendance",
                        "Today's class roll and attendance history.",
                        "Open",
                        "#E8604F",
                        "#FAECE7",
                        this::handleMarkAttendance
                ),
                buildActionCard(
                        "M",
                        "Media lessons",
                        "Images and video files wired into topics.",
                        "Browse",
                        "#2FB6A6",
                        "#E1F5EE",
                        this::handleMediaLesson
                ),
                buildActionCard(
                        "S",
                        "Enter scores",
                        "Test and exam entries with final score rollup.",
                        "Review",
                        "#F2A93D",
                        "#412402",
                        this::handleEnterScores
                ),
                buildActionCard(
                        "Y",
                        "Sync data",
                        "USB export or import for offline reconciliation.",
                        "Open",
                        "#2A2F5C",
                        "#FBF3E4",
                        this::handleSyncData
                )
        );

        return actionGrid;
    }

    private HBox buildLowerSection(DashboardStats stats) {
        VBox mediaPanel = buildMediaPanel(stats);
        VBox attendancePanel = buildAttendancePanel(stats);

        HBox lowerSection = new HBox(18, mediaPanel, attendancePanel);
        HBox.setHgrow(mediaPanel, Priority.ALWAYS);
        HBox.setHgrow(attendancePanel, Priority.NEVER);
        return lowerSection;
    }

    private VBox buildMediaPanel(DashboardStats stats) {
        Label title = new Label("Media spotlight");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Tap a card to launch the linked file with the desktop app.");
        subtitle.getStyleClass().add("section-subtitle");

        FlowPane cards = new FlowPane();
        cards.setHgap(12);
        cards.setVgap(12);
        cards.setPrefWrapLength(780);

        if (stats.mediaTopics().isEmpty()) {
            Label empty = new Label("No media topics are configured.");
            empty.getStyleClass().add("empty-state");
            cards.getChildren().add(empty);
        } else {
            for (MediaTopicPreview preview : stats.mediaTopics()) {
                cards.getChildren().add(buildMediaCard(preview));
            }
        }

        VBox panel = new VBox(12, title, subtitle, cards);
        panel.getStyleClass().add("dashboard-panel");
        panel.setFillWidth(true);
        return panel;
    }

    private VBox buildAttendancePanel(DashboardStats stats) {
        Label title = new Label("Attendance snapshot");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("PRESENT and LATE count as attended in the current engine.");
        subtitle.getStyleClass().add("section-subtitle");

        Label rate = new Label(formatPercentage(stats.attendanceRate()));
        rate.getStyleClass().add("attendance-rate");

        Label total = new Label(stats.attendanceRecords() + " attendance records");
        total.getStyleClass().add("attendance-total");

        ProgressBar progressBar = new ProgressBar(Math.max(0.0, Math.min(1.0, stats.attendanceRate() / 100.0)));
        progressBar.getStyleClass().add("attendance-progress");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        HBox counts = new HBox(10,
                buildCountChip("PRESENT", stats.presentCount(), "chip-teal"),
                buildCountChip("LATE", stats.lateCount(), "chip-gold"),
                buildCountChip("ABSENT", stats.absentCount(), "chip-coral")
        );

        Label scoreSummary = new Label(
                stats.scoreCount() == 0
                        ? "No score entries recorded yet."
                        : String.format(Locale.US, "Scores: %d records, average %.1f", stats.scoreCount(), stats.averageFinalScore())
        );
        scoreSummary.getStyleClass().add("attendance-note");

        VBox panel = new VBox(12, title, subtitle, rate, total, progressBar, counts, scoreSummary);
        panel.getStyleClass().add("dashboard-panel");
        panel.setPrefWidth(320);
        return panel;
    }

    private VBox buildStatCard(String label, String value, String subtitle, String accentColor) {
        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("stat-label");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("stat-subtitle");

        VBox card = new VBox(4, titleLabel, valueLabel, subtitleLabel);
        card.getStyleClass().add("dashboard-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setStyle("-fx-border-color: " + accentColor + " transparent transparent transparent; -fx-border-width: 3 0 0 0;");
        return card;
    }

    private VBox buildActionCard(
            String iconText,
            String title,
            String subtitle,
            String buttonText,
            String accentColor,
            String iconColor,
            Runnable onAction
    ) {
        Label icon = new Label(iconText);
        icon.getStyleClass().add("action-icon");
        icon.setStyle("-fx-background-color: " + accentColor + "; -fx-text-fill: " + iconColor + ";");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("action-subtitle");

        Button button = new Button(buttonText);
        button.getStyleClass().add("action-button");
        button.setStyle("-fx-text-fill: " + accentColor + ";");
        button.setOnAction(event -> onAction.run());

        VBox card = new VBox(10, icon, titleLabel, subtitleLabel, button);
        card.getStyleClass().addAll("action-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setStyle("-fx-background-color: " + accentColor + ";");
        return card;
    }

    private VBox buildMediaCard(MediaTopicPreview preview) {
        Label subject = new Label(preview.subjectName());
        subject.getStyleClass().add("media-subject");

        Label title = new Label(preview.title());
        title.getStyleClass().add("media-title");
        title.setWrapText(true);

        Label fileName = new Label(preview.fileName());
        fileName.getStyleClass().add("media-file");
        fileName.setWrapText(true);

        Label badge = new Label(preview.mediaKind());
        badge.getStyleClass().addAll("media-badge", mediaBadgeClass(preview.mediaKind()));

        Button openButton = new Button("Open");
        openButton.getStyleClass().add("media-open-button");
        openButton.setOnAction(event -> launchMedia(preview, openButton));

        VBox card = new VBox(8, subject, title, badge, fileName, openButton);
        card.getStyleClass().add("media-card");
        card.setPrefWidth(240);
        return card;
    }

    private Node buildCountChip(String label, long count, String styleClass) {
        Label chipLabel = new Label(label + " " + count);
        chipLabel.getStyleClass().addAll("count-chip", styleClass);
        return chipLabel;
    }

    private void launchMedia(MediaTopicPreview preview, Button launchButton) {
        launchMediaAsync(
                preview.subjectName(),
                preview.mediaPath(),
                preview.fileName(),
                "Media unavailable",
                "Unable to launch " + preview.fileName() + ".",
                launchButton,
                null
        );
    }

    private void showInfo(String title, String body) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(body);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        alert.showAndWait();
    }

    private String formatPercentage(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    private String mediaBadgeClass(String mediaKind) {
        return switch (mediaKind) {
            case "VIDEO" -> "badge-video";
            case "IMAGE" -> "badge-image";
            default -> "badge-file";
        };
    }

    private void handleMarkAttendance() {
        try {
            List<Pupil> pupils = pupilDAO.findByClassLevel(PRIMARY_FIVE);
            if (pupils.isEmpty()) {
                showError("Mark attendance", "No pupils are registered for " + PRIMARY_FIVE + ".");
                return;
            }

            Optional<Pupil> selectedPupil = chooseItemDialog(
                    "Mark attendance",
                    "Select the pupil to mark attendance for.",
                    pupils,
                    pupil -> pupil.getName() + " (ID " + pupil.getPupilId() + ")"
            );
            if (selectedPupil.isEmpty()) {
                return;
            }

            Optional<LocalDate> selectedDate = promptDate("Attendance date", "Choose the attendance date.", LocalDate.now());
            if (selectedDate.isEmpty()) {
                return;
            }

            Optional<String> selectedStatus = chooseStringDialog(
                    "Attendance status",
                    "Choose the attendance status.",
                    List.of("PRESENT", "LATE", "ABSENT"),
                    "PRESENT"
            );
            if (selectedStatus.isEmpty()) {
                return;
            }

            attendanceEngine.recordAttendance(
                    selectedPupil.get().getPupilId(),
                    selectedDate.get(),
                    selectedStatus.get()
            );

            showInfo(
                    "Attendance recorded",
                    selectedPupil.get().getName() + " was marked " + selectedStatus.get()
                            + " on " + selectedDate.get() + "."
            );
        } catch (RuntimeException exception) {
            showError("Mark attendance failed", exception.getMessage());
        }
    }

    private void handleEnterScores() {
        try {
            List<Pupil> pupils = pupilDAO.findByClassLevel(PRIMARY_FIVE);
            List<Subject> subjects = subjectDAO.findAll();

            if (pupils.isEmpty()) {
                showError("Enter scores", "No pupils are registered for " + PRIMARY_FIVE + ".");
                return;
            }
            if (subjects.isEmpty()) {
                showError("Enter scores", "No subjects are configured.");
                return;
            }

            Optional<Pupil> selectedPupil = chooseItemDialog(
                    "Enter scores",
                    "Select the pupil to score.",
                    pupils,
                    pupil -> pupil.getName() + " (ID " + pupil.getPupilId() + ")"
            );
            if (selectedPupil.isEmpty()) {
                return;
            }

            Optional<Subject> selectedSubject = chooseItemDialog(
                    "Enter scores",
                    "Select the subject.",
                    subjects,
                    Subject::getName
            );
            if (selectedSubject.isEmpty()) {
                return;
            }

            Optional<String> sessionInput = promptText(
                    "Academic session",
                    "Enter the academic session in YYYY/YYYY format.",
                    defaultAcademicSession()
            );
            if (sessionInput.isEmpty()) {
                return;
            }
            AcademicSession session = AcademicSession.parse(sessionInput.get());

            Optional<String> selectedTerm = chooseStringDialog(
                    "Academic term",
                    "Select the term.",
                    AcademicTerm.displayNames(),
                    AcademicTerm.FIRST.getDisplayName()
            );
            if (selectedTerm.isEmpty()) {
                return;
            }

            Optional<Double> testScore = promptDecimal(
                    "Test score",
                    "Enter the test score between 0 and 40.",
                    20.0,
                    0.0,
                    40.0
            );
            if (testScore.isEmpty()) {
                return;
            }

            Optional<Double> examScore = promptDecimal(
                    "Exam score",
                    "Enter the exam score between 0 and 60.",
                    40.0,
                    0.0,
                    60.0
            );
            if (examScore.isEmpty()) {
                return;
            }

            double finalScore = gradingEngine.computeTotal(testScore.get(), examScore.get());
            String grade = gradingEngine.assignGrade(finalScore);
            String sessionValue = session.toString();
            String termValue = selectedTerm.get();

            Score existingScore = scoreDAO.findByPupilSubjectTerm(
                    selectedPupil.get().getPupilId(),
                    selectedSubject.get().getSubjectId(),
                    sessionValue,
                    termValue
            );

            if (existingScore == null) {
                Score score = new Score(
                        0,
                        selectedPupil.get().getPupilId(),
                        selectedSubject.get().getSubjectId(),
                        sessionValue,
                        termValue,
                        testScore.get(),
                        examScore.get(),
                        finalScore,
                        grade
                );
                scoreDAO.create(score);
            } else {
                existingScore.setTestScore(testScore.get());
                existingScore.setExamScore(examScore.get());
                existingScore.setFinalScore(finalScore);
                existingScore.setGrade(grade);
                scoreDAO.update(existingScore);
            }

            showInfo(
                    "Score saved",
                    selectedPupil.get().getName() + " - " + selectedSubject.get().getName()
                            + " = " + String.format(Locale.US, "%.1f", finalScore)
                            + " (" + grade + ")"
            );
        } catch (RuntimeException exception) {
            showError("Enter scores failed", exception.getMessage());
        }
    }

    private void handleMediaLesson() {
        try {
            List<Subject> subjects = subjectDAO.findAll();
            if (subjects.isEmpty()) {
                showError("Media lessons", "No subjects are configured.");
                return;
            }

            Optional<Subject> selectedSubject = chooseItemDialog(
                    "Media lessons",
                    "Choose the subject you want to open.",
                    subjects,
                    Subject::getName
            );
            if (selectedSubject.isEmpty()) {
                return;
            }

            Optional<String> selectedTerm = chooseStringDialog(
                    "Media lessons",
                    "Choose the term that contains the media topic.",
                    AcademicTerm.displayNames(),
                    AcademicTerm.FIRST.getDisplayName()
            );
            if (selectedTerm.isEmpty()) {
                return;
            }

            List<Topic> topics = topicDAO.findBySubjectClassLevelTerm(
                    selectedSubject.get().getSubjectId(),
                    PRIMARY_FIVE,
                    selectedTerm.get()
            );
            if (topics.isEmpty()) {
                showError(
                        "Media lessons",
                        "No topics were found for " + selectedSubject.get().getName() + " in " + selectedTerm.get() + "."
                );
                return;
            }

            Optional<Topic> selectedTopic = chooseItemDialog(
                    "Media lessons",
                    "Choose the topic to launch.",
                    topics,
                    Topic::getTitle
            );
            if (selectedTopic.isEmpty()) {
                return;
            }

            String mediaPath = selectedTopic.get().getMediaPath();
            if (mediaPath == null || mediaPath.isBlank()) {
                showError("Media lessons", "No media file is configured for " + selectedTopic.get().getTitle() + ".");
                return;
            }

            launchMediaAsync(
                    selectedSubject.get().getName(),
                    mediaPath,
                    selectedTopic.get().getTitle(),
                    "Media lessons",
                    "The selected media file is not launchable: " + mediaPath,
                    null,
                    () -> showInfo(
                            "Media launched",
                            selectedSubject.get().getName() + " - " + selectedTopic.get().getTitle()
                                    + System.lineSeparator() + mediaPath
                    )
            );
        } catch (RuntimeException exception) {
            showError("Media lessons failed", exception.getMessage());
        }
    }

    private void handleSyncData() {
        try {
            Optional<String> mode = chooseStringDialog(
                    "Sync data",
                    "Choose whether to export or import data.",
                    List.of("Export", "Import"),
                    "Export"
            );
            if (mode.isEmpty()) {
                return;
            }

            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(mode.get() + " data folder");
            if (primaryStage != null) {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home", ".")));
            }
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory == null) {
                return;
            }

            if ("Export".equals(mode.get())) {
                syncExporter.export(selectedDirectory.getAbsolutePath());
                showInfo("Sync data", "Export completed in " + selectedDirectory.getAbsolutePath() + ".");
            } else {
                syncImporter.importFrom(selectedDirectory.getAbsolutePath());
                showInfo("Sync data", "Import completed from " + selectedDirectory.getAbsolutePath() + ".");
            }
        } catch (RuntimeException exception) {
            showError("Sync data failed", exception.getMessage());
        }
    }

    private void showError(String title, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(body);
        if (primaryStage != null) {
            alert.initOwner(primaryStage);
        }
        alert.showAndWait();
    }

    private void launchMediaAsync(
            String subjectName,
            String mediaPath,
            String displayLabel,
            String errorTitle,
            String errorBody,
            Button launchButton,
            Runnable onSuccess
    ) {
        if (!mediaLauncher.isLaunchable(subjectName, mediaPath)) {
            showError(errorTitle, errorBody);
            return;
        }

        String originalButtonText = launchButton == null ? null : launchButton.getText();
        if (launchButton != null) {
            launchButton.setDisable(true);
            launchButton.setText("Opening...");
        }

        Thread launcherThread = new Thread(() -> {
            boolean launched;
            try {
                launched = mediaLauncher.launch(subjectName, mediaPath);
            } catch (RuntimeException exception) {
                launched = false;
            }

            boolean finalLaunched = launched;
            Platform.runLater(() -> {
                if (launchButton != null) {
                    launchButton.setDisable(false);
                    launchButton.setText(originalButtonText);
                }

                if (finalLaunched) {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } else {
                    showError(errorTitle, errorBody.isBlank()
                            ? "Unable to launch " + displayLabel + "."
                            : errorBody);
                }
            });
        }, "media-launch-" + displayLabel.replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase(Locale.ROOT));
        launcherThread.setDaemon(true);
        launcherThread.start();
    }

    private Optional<String> promptText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }

        return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank());
    }

    private Optional<Double> promptDecimal(String title, String header, double defaultValue, double min, double max) {
        TextInputDialog dialog = new TextInputDialog(String.format(Locale.US, "%.1f", defaultValue));
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }

        Optional<String> response = dialog.showAndWait();
        if (response.isEmpty()) {
            return Optional.empty();
        }

        try {
            double value = Double.parseDouble(response.get().trim());
            if (value < min || value > max) {
                showError(title, "Enter a value between " + min + " and " + max + ".");
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (NumberFormatException exception) {
            showError(title, "Enter a valid number.");
            return Optional.empty();
        }
    }

    private Optional<LocalDate> promptDate(String title, String header, LocalDate defaultValue) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }

        DatePicker picker = new DatePicker(defaultValue);
        picker.setMaxWidth(Double.MAX_VALUE);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(new Label("Date"), 0, 0);
        gridPane.add(picker, 1, 0);

        dialog.getDialogPane().setContent(gridPane);
        ButtonBar.ButtonData okButtonData = ButtonBar.ButtonData.OK_DONE;
        ButtonType okButton = new ButtonType("Continue", okButtonData);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == okButton ? picker.getValue() : null);

        return dialog.showAndWait();
    }

    private Optional<String> chooseStringDialog(String title, String header, List<String> options, String defaultChoice) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, options);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }
        return dialog.showAndWait();
    }

    private <T> Optional<T> chooseItemDialog(
            String title,
            String header,
            List<T> items,
            Function<T, String> displayFormatter
    ) {
        if (items.isEmpty()) {
            return Optional.empty();
        }

        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        if (primaryStage != null) {
            dialog.initOwner(primaryStage);
        }

        ButtonType okButton = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        ComboBox<T> comboBox = new ComboBox<>(FXCollections.observableArrayList(items));
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFormatter.apply(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFormatter.apply(item));
            }
        });
        comboBox.getSelectionModel().selectFirst();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(new Label("Selection"), 0, 0);
        gridPane.add(comboBox, 1, 0);
        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(button -> button == okButton ? comboBox.getValue() : null);

        return dialog.showAndWait();
    }

    private String defaultAcademicSession() {
        int year = Year.now().getValue();
        return year + "/" + (year + 1);
    }
}
