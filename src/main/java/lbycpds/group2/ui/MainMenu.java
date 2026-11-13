package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lbycpds.group2.logic.TextFileQuestionBank;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class MainMenu {

    TextFileQuestionBank circuits = new TextFileQuestionBank("/questions/circuits.txt");
    TextFileQuestionBank dsa = new TextFileQuestionBank("/questions/dsa.txt");

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

        HBox topBar = new HBox(logo, spacer, welcome);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        // Study mode
        RadioButton regular = new RadioButton();
        RadioButton cram = new RadioButton();
        ToggleGroup group = new ToggleGroup();
        regular.setToggleGroup(group);
        cram.setToggleGroup(group);

        HBox regularCard = modeCard("REGULAR MODE (ONE COURSE)",
                "Prioritize learning on a specific subject. Please select your course on the right.", regular);
        HBox cramCard = modeCard("CRAM MODE (ALL COURSES)",
                "A rapid-fire review across all courses. Godspeed!", cram);
        regular.setSelected(true);

        Label modeHeader = new Label("CHOOSE YOUR STUDY MODE");
        modeHeader.getStyleClass().add("card-label");

        VBox modeCardBox = new VBox(14, modeHeader, regularCard, cramCard);
        modeCardBox.getStyleClass().add("card");

        Label modeTitle = new Label("Study Mode");
        modeTitle.getStyleClass().add("section-title");
        VBox leftColumn = new VBox(12, modeTitle, modeCardBox);

        // Course & action
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.getItems().addAll(circuits.getCourse(), dsa.getCourse());
        courseBox.setValue(circuits.getCourse());
        courseBox.setMaxWidth(Double.MAX_VALUE);
        courseBox.disableProperty().bind(cram.selectedProperty());

        Button start = new Button("START SESSION  ");
        start.getStyleClass().add("start-button");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(e -> {
            AppState.topics = new ArrayList<>();

            if (cram.isSelected()) {
                AppState.mode = "CRAM";
                AppState.course = null;
                AppState.student.addCourse(circuits.getCourse());
                AppState.student.addCourse(dsa.getCourse());
                AppState.topics.addAll(loadTopics(circuits));
                AppState.topics.addAll(loadTopics(dsa));
            } else {
                AppState.mode = "REGULAR";
                AppState.course = courseBox.getValue();
                AppState.student.addCourse(AppState.course);
                if (AppState.course.equals(circuits.getCourse())) {
                    AppState.topics.addAll(loadTopics(circuits));
                } else {
                    AppState.topics.addAll(loadTopics(dsa));
                }
            }

            // Confidence screen build
            SceneManager.show(new ConfidenceScreen().build());
        });

        Label courseHeader = new Label("SELECT YOUR COURSE");
        courseHeader.getStyleClass().add("card-label");

        VBox courseCardBox = new VBox(20, courseHeader, courseBox, start);
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
    private HBox modeCard(String title, String desc, RadioButton radio) {
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

        // Highlight the selected study mode
        radio.selectedProperty().addListener((obs, was, is) -> {
            if (is) card.getStyleClass().add("selected");
            else card.getStyleClass().remove("selected");
        });
        return card;
    }

    // Makes a Topic for each unique topic name in the question bank
    private List<Topic> loadTopics(TextFileQuestionBank bank) {
        List<Topic> list = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (Question q : bank.loadQuestions()) {
            if (!seen.contains(q.getTopic())) {
                seen.add(q.getTopic());
                list.add(new Topic(q.getTopic(), bank.getCourse()));
            }
        }
        return list;
    }
}