package lbycpds.group2.ui;

import lbycpds.group2.logic.LessonRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lbycpds.group2.logic.QuestionBankFiles;
import lbycpds.group2.logic.StudyFlow;
import lbycpds.group2.logic.TextFileQuestionBank;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Topic;
import java.util.ArrayList;
import java.util.List;
import lbycpds.group2.StudySessionController;
import lbycpds.group2.database.LastSession;
import lbycpds.group2.database.ProgressRestorer;
import lbycpds.group2.logic.AdaptiveDifficulty;
import lbycpds.group2.logic.SingleCourseStudySession;
import lbycpds.group2.database.Stores;

public class MainMenu {

    TextFileQuestionBank circuits = QuestionBankFiles.open("/questions/circuits.txt");
    TextFileQuestionBank dsa = QuestionBankFiles.open("/questions/dsa.txt");

    public Parent build() {
        // Top bar
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");

        Label welcome = new Label();
        if (AppState.returning) {
            welcome.setText("Welcome back, " + AppState.student.getName() + "!");
        } else {
            welcome.setText("Hello, " + AppState.student.getName() + "!");
        }
        welcome.getStyleClass().add("user-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("Log out");
        logout.getStyleClass().add("small-button");
        logout.setOnAction(e -> {
            AppState.reset();
            SceneManager.show(new RoleScreen().build());
        });

        HBox topBar = new HBox(16, logo, spacer, welcome, logout);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        // Study mode
        RadioButton regular = new RadioButton();
        RadioButton cram = new RadioButton();
        ToggleGroup group = new ToggleGroup();
        regular.setToggleGroup(group);
        cram.setToggleGroup(group);
        RadioButton tutor = new RadioButton();
        tutor.setToggleGroup(group);

        HBox regularCard = modeCard("REGULAR MODE (ONE COURSE)",
                "Prioritize learning on a specific subject. Please select your course on the right.", regular);
        HBox cramCard = modeCard("CRAM MODE (ONE COURSE)",
                "A jumbled quiz of every topic in the course, one level at a time. Pass it to unlock the next level.", cram);
        HBox tutorCard = modeCard("TUTOR MODE (ONE COURSE)",
                "Learn with the answer keys. Try a question, check it, or reveal the answer. Nothing is graded.", tutor);
        regular.setSelected(true);

        Label modeHeader = new Label("CHOOSE YOUR STUDY MODE");
        modeHeader.getStyleClass().add("card-label");

        VBox modeCardBox = new VBox(14, modeHeader, regularCard, cramCard, tutorCard);
        modeCardBox.getStyleClass().add("card");

        Label modeTitle = new Label("Study Mode");
        modeTitle.getStyleClass().add("section-title");
        VBox leftColumn = new VBox(12, modeTitle, modeCardBox);

        // Course & action
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.getItems().addAll(circuits.getCourse(), dsa.getCourse());
        courseBox.setValue(circuits.getCourse());
        courseBox.setMaxWidth(Double.MAX_VALUE);

        Button start = new Button("START SESSION  ");
        start.getStyleClass().add("start-button");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(e -> {
            if (tutor.isSelected()) {
                boolean isCircuits = courseBox.getValue().equals(circuits.getCourse());
                TextFileQuestionBank bank = isCircuits ? circuits : dsa;
                LessonRepository lessons = LessonRepository.forQuestionBank(
                        isCircuits ? "/questions/circuits.txt" : "/questions/dsa.txt");
                SceneManager.show(new TutorTopicScreen(bank.getCourse(), lessons, bank.loadQuestions()).build());
                return;
            }

            AppState.topics = new ArrayList<>();

            AppState.mode = cram.isSelected() ? "CRAM" : "REGULAR";
            AppState.course = courseBox.getValue();
            AppState.student.addCourse(AppState.course);
            if (AppState.course.equals(circuits.getCourse())) {
                AppState.topics.addAll(loadTopics(circuits));
            } else {
                AppState.topics.addAll(loadTopics(dsa));
            }

            AppState.graphs = StudyFlow.buildGraphs(AppState.topics, List.of(circuits, dsa));

            if (cram.isSelected()) {
                StudyRunner.start();
                return;
            }

            // Returning students can keep their saved confidence ratings
            if (hasSavedRatings() && keepSavedRatings()) {
                fillMissingRatings();
                StudyRunner.start();
                return;
            }

            // Confidence screen build
            SceneManager.show(new ConfidenceScreen().build());
        });

        Label courseHeader = new Label("SELECT YOUR COURSE");
        courseHeader.getStyleClass().add("card-label");

        VBox courseCardBox = new VBox(20, courseHeader, courseBox, start);

        // Returning students can pick up where they stopped, in the mode they were using
        LastSession resume = findSavedSession();
        if (resume != null) {
            Button resumeButton = new Button("CONTINUE LAST SESSION");
            resumeButton.getStyleClass().add("start-button");
            resumeButton.setMaxWidth(Double.MAX_VALUE);
            Label resumeInfo = new Label(describe(resume));
            resumeInfo.getStyleClass().add("mode-desc");
            resumeInfo.setWrapText(true);
            resumeButton.setOnAction(e -> continueLast(resume));
            courseCardBox.getChildren().addAll(resumeInfo, resumeButton);
        }
        courseCardBox.getStyleClass().add("card");

        Label courseTitle = new Label("Course & Action");
        courseTitle.getStyleClass().add("section-title");
        VBox rightColumn = new VBox(12, courseTitle, courseCardBox);

        // Layout
        HBox columns = new HBox(30, leftColumn, rightColumn);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        leftColumn.setMaxWidth(Double.MAX_VALUE);
        rightColumn.setMaxWidth(Double.MAX_VALUE);
        leftColumn.setPrefWidth(1);   // equal widths
        rightColumn.setPrefWidth(1);
        columns.setPadding(new Insets(30, 40, 30, 40));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(columns);
        root.getStyleClass().add("green");
        return root;
    }

    // Study modes
    static HBox modeCard(String title, String desc, RadioButton radio) {
        Label t = new Label(title);
        t.getStyleClass().add("mode-title");
        Label d = new Label(desc);
        d.getStyleClass().add("mode-desc");
        d.setWrapText(true);

        VBox text = new VBox(4, t, d);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox card = new HBox(12, text, radio);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("mode-card");

        card.setOnMouseClicked(e -> radio.setSelected(true));

        radio.selectedProperty().addListener((obs, was, is) -> {
            if (is) card.getStyleClass().add("selected");
            else card.getStyleClass().remove("selected");
        });
        return card;
    }

    // The student's saved session from an earlier run
    private LastSession findSavedSession() {
        if (!AppState.returning || !Stores.isConnected()) {
            return null;
        }
        String name = AppState.student.getName();
        try {
            if (Stores.lastSessionStore() != null) {
                LastSession last = Stores.lastSessionStore().load(name);
                if (last != null) {
                    return last;
                }
            }
            // Older saves only know the course and topic of a regular session
            StudySessionController controller = new StudySessionController(Stores.topicStore(),
                    Stores.sessionStore(), Stores.historyStore(), new AdaptiveDifficulty());
            SingleCourseStudySession regular = controller.continueLastSession(AppState.student, null);
            if (regular == null) {
                return null;
            }
            return new LastSession(regular.nextCourse(), "REGULAR",
                    Stores.topicStore().loadLastTopic(name, regular.nextCourse()));
        } catch (RuntimeException e) {
            System.out.println("Could not look for a saved session: " + e.getMessage());
            return null;
        }
    }

    // Text under the Continue button: the mode, the course, and the topic or cram level
    private String describe(LastSession last) {
        if (last.isCram()) {
            String level = "";
            try {
                if (Stores.cramStore() != null) {
                    level = "  -  Level " + Stores.cramStore().loadLevel(AppState.student.getName(), last.getCourse());
                }
            } catch (RuntimeException e) {
                level = "";
            }
            return "CRAM MODE  -  " + last.getCourse() + level;
        }
        return "REGULAR MODE  -  " + last.getCourse()
                + (last.getTopic() != null ? "  -  " + last.getTopic() : "");
    }

    // Loads the saved progress and goes straight back into the mode the student was using
    private void continueLast(LastSession last) {
        AppState.mode = last.isCram() ? "CRAM" : "REGULAR";
        AppState.course = last.getCourse();
        AppState.student.addCourse(last.getCourse());
        AppState.topics = new ArrayList<>();
        AppState.topics.addAll(loadTopics(last.getCourse().equals(circuits.getCourse()) ? circuits : dsa));
        AppState.graphs = StudyFlow.buildGraphs(AppState.topics, List.of(circuits, dsa));
        if (last.isCram()) {
            StudyRunner.start();
            return;
        }
        fillMissingRatings();
        AppState.resumeTopic = last.getTopic();
        StudyRunner.start();
    }

    private boolean hasSavedRatings() {
        if (!AppState.returning) {
            return false;
        }
        for (Topic topic : AppState.topics) {
            if (topic.isUnlocked() && topic.getConfidence() > 0) {
                return true;
            }
        }
        return false;
    }

    // Asks the student whether to keep the saved ratings or rate the topics again
    private boolean keepSavedRatings() {
        return Dialogs.ask("You already rated your topics before. Keep those confidence ratings?",
                "Keep saved ratings", "Change ratings") == 0;
    }

    private void fillMissingRatings() {
        for (Topic topic : AppState.topics) {
            if (topic.isUnlocked() && topic.getConfidence() <= 0) {
                topic.setConfidence(3);
                if (Stores.isConnected()) {
                    Stores.topicStore().saveTopic(AppState.student.getName(), topic);
                }
            }
        }
    }

    // Makes a Topic for each topic name in the question bank
    private List<Topic> loadTopics(TextFileQuestionBank bank) {
        List<Topic> list = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (Question q : bank.loadQuestions()) {
            if (!seen.contains(q.getTopic())) {
                seen.add(q.getTopic());
                list.add(new Topic(q.getTopic(), bank.getCourse()));
            }
        }
        if (AppState.returning && Stores.isConnected()) {
            ProgressRestorer.restore(Stores.topicStore(), AppState.student.getName(), list);
        }
        return list;
    }
}