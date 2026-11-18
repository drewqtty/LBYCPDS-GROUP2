package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.logic.TutorQuestionList;
import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.ProblemSolvingQuestion;
import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;

public class TutorScreen {

    private static final String ALL_TOPICS = "All topics";

    private final String course;
    private final Question[] allQuestions;
    private final List<String> topics;

    private String topicFilter = ALL_TOPICS;
    private List<Question> questions;
    private int index = 0;

    private ToggleGroup choiceGroup;
    private TextField answerField;

    public TutorScreen(String course, Question[] allQuestions) {
        this.course = course;
        this.allQuestions = allQuestions;
        this.topics = TutorQuestionList.topics(allQuestions);
        this.questions = TutorQuestionList.forTopic(allQuestions, null);
    }

    public Parent build() {
        choiceGroup = null;
        answerField = null;

        // Top bar
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");
        Label mode = new Label("Tutor Mode  |  " + course);
        mode.getStyleClass().add("user-name");
        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        HBox topBar = new HBox(16, logo, topSpacer, mode);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        // Topic filter
        ComboBox<String> topicBox = new ComboBox<>();
        topicBox.getItems().add(ALL_TOPICS);
        topicBox.getItems().addAll(topics);
        topicBox.setValue(topicFilter);
        topicBox.setMaxWidth(Double.MAX_VALUE);
        topicBox.setOnAction(e -> {
            topicFilter = topicBox.getValue();
            questions = TutorQuestionList.forTopic(allQuestions,
                    ALL_TOPICS.equals(topicFilter) ? null : topicFilter);
            index = 0;
            SceneManager.show(build());
        });

        Button back = new Button("Back to menu");
        back.setOnAction(e -> SceneManager.show(new MainMenu().build()));

        HBox filterRow = new HBox(12, topicBox, back);
        HBox.setHgrow(topicBox, Priority.ALWAYS);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        if (questions.isEmpty()) {
            Label none = new Label("There are no questions in this topic yet.");
            none.getStyleClass().add("quiz-question");
            VBox empty = new VBox(14, filterRow, none);
            empty.setPadding(new Insets(20, 40, 24, 40));
            BorderPane emptyRoot = new BorderPane();
            emptyRoot.setTop(topBar);
            emptyRoot.setCenter(empty);
            emptyRoot.getStyleClass().add("green");
            return emptyRoot;
        }

        Question q = questions.get(index);

        Label position = new Label("QUESTION " + (index + 1) + " OF " + questions.size());
        position.getStyleClass().add("card-label");
        Label info = new Label(q.getTopic() + "  •  Level " + q.getDifficulty());
        info.getStyleClass().add("mode-desc");

        // Question card
        Label text = new Label(clean(q.getText()));
        text.getStyleClass().add("quiz-question");
        text.setWrapText(true);
        text.setMaxWidth(Double.MAX_VALUE);
        text.setMinHeight(Region.USE_PREF_SIZE);

        VBox answerArea = buildAnswerArea(q);

        Label feedback = new Label("");
        feedback.setWrapText(true);
        feedback.setMaxWidth(Double.MAX_VALUE);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        feedback.getStyleClass().add("quiz-feedback");

        Label key = new Label("");
        key.setWrapText(true);
        key.setMaxWidth(Double.MAX_VALUE);
        key.setMinHeight(Region.USE_PREF_SIZE);
        key.getStyleClass().add("quiz-feedback");

        Button check = new Button("Check my answer");
        Button reveal = new Button("Show answer key");
        reveal.getStyleClass().add("start-button");
        HBox tutorButtons = new HBox(12, check, reveal);

        check.setOnAction(e -> {
            String answer = readAnswer();
            if (answer.isBlank()) {
                setStyle(feedback, "wrong", "Enter or choose an answer first, or press Show answer key.");
            } else if (q.checkAnswer(answer)) {
                setStyle(feedback, "correct", "Correct!");
            } else {
                setStyle(feedback, "wrong", "Not quite. Try again, or press Show answer key.");
            }
        });

        reveal.setOnAction(e -> {
            setStyle(key, "correct", "Answer key: " + describeAnswer(q) + keyPoints(q));
            highlightCorrectChoice(q, answerArea);
            reveal.setDisable(true);
        });

        VBox inner = new VBox(14, text, answerArea, tutorButtons, feedback, key);
        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Previous / next
        Button previous = new Button("Previous");
        previous.setDisable(index == 0);
        previous.setOnAction(e -> {
            index--;
            SceneManager.show(build());
        });

        Button next = new Button(index + 1 < questions.size() ? "Next" : "Back to start");
        next.getStyleClass().add("start-button");
        next.setOnAction(e -> {
            index = index + 1 < questions.size() ? index + 1 : 0;
            SceneManager.show(build());
        });

        if (answerField != null) {
            answerField.setOnAction(e -> check.fire());
        }

        Region navSpacer = new Region();
        HBox.setHgrow(navSpacer, Priority.ALWAYS);
        HBox nav = new HBox(15, previous, navSpacer, next);
        nav.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, filterRow, position, info, card, nav);
        content.setPadding(new Insets(20, 40, 24, 40));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(content);
        root.getStyleClass().add("green");
        return root;
    }

    private VBox buildAnswerArea(Question q) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);

        if (q instanceof MultipleChoiceQuestion mc) {
            choiceGroup = new ToggleGroup();
            String[] choices = mc.getChoices();
            for (int i = 0; i < choices.length; i++) {
                char letter = (char) ('A' + i);
                RadioButton rb = new RadioButton(letter + ".  " + clean(choices[i]));
                rb.setToggleGroup(choiceGroup);
                rb.setWrapText(true);
                rb.setMnemonicParsing(false);
                rb.getStyleClass().add("quiz-choice");
                rb.setUserData(String.valueOf(letter));
                box.getChildren().add(rb);
            }
        } else {
            answerField = new TextField();
            answerField.setMaxWidth(420);
            answerField.setPromptText("PROBLEM_SOLVING".equals(q.getType())
                    ? "Type your answer (plain numbers, no units)"
                    : "Type your answer");
            box.getChildren().add(answerField);
        }
        return box;
    }

    private String readAnswer() {
        if (choiceGroup != null) {
            return choiceGroup.getSelectedToggle() == null
                    ? "" : (String) choiceGroup.getSelectedToggle().getUserData();
        }
        return answerField.getText() == null ? "" : answerField.getText().trim();
    }

    // Colors the correct choice green once the answer key is shown
    private void highlightCorrectChoice(Question q, VBox answerArea) {
        if (!(q instanceof MultipleChoiceQuestion)) {
            return;
        }
        String letter = q.getAnswer().trim().toUpperCase();
        for (javafx.scene.Node node : answerArea.getChildren()) {
            if (node instanceof RadioButton rb && letter.equals(rb.getUserData())) {
                rb.setStyle("-fx-text-fill: #5CE0A0; -fx-font-weight: bold;");
            }
        }
    }

    private String describeAnswer(Question q) {
        if (q instanceof MultipleChoiceQuestion mc && !q.getAnswer().isBlank()) {
            String letter = q.getAnswer().trim().toUpperCase();
            int idx = letter.charAt(0) - 'A';
            if (idx >= 0 && idx < mc.getChoices().length) {
                return letter + ".  " + clean(mc.getChoices()[idx]);
            }
        }
        return q.getAnswer();
    }

    private String keyPoints(Question q) {
        if (q instanceof ProblemSolvingQuestion ps && ps.getKeywords().length > 0) {
            List<String> words = new ArrayList<>();
            for (String keyword : ps.getKeywords()) {
                words.add(keyword);
            }
            return "\nA full answer mentions: " + String.join(", ", words);
        }
        return "";
    }

    private static String clean(String s) {
        return s.replace("\\Delta", "Δ").replace("\\pi", "π").replace("$", "");
    }

    private void setStyle(Label label, String styleClass, String message) {
        label.getStyleClass().setAll("label", "quiz-feedback", styleClass);
        label.setText(message);
    }
}
