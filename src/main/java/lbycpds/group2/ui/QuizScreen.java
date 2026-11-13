package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class QuizScreen {

    public static class Result {
        public final int total;
        public final int correct;
        public final int seconds;
        public final List<Question> missed;

        Result(int total, int correct, int seconds, List<Question> missed) {
            this.total = total;
            this.correct = correct;
            this.seconds = seconds;
            this.missed = missed;
        }

        public int percent() {
            return total == 0 ? 0 : Math.round(100f * correct / total);
        }
    }

    private final List<Question> questions;
    private final Consumer<Result> onFinish;

    private int index = 0;
    private int correct = 0;
    private final List<Question> missed = new ArrayList<>();
    private final long startMillis = System.currentTimeMillis();

    // per-question UI state
    private ToggleGroup choiceGroup;
    private TextField answerField;
    private boolean submitted;

    public QuizScreen(List<Question> questions, Consumer<Result> onFinish) {
        this.questions = questions;
        this.onFinish = onFinish;
    }


    public Parent build() {
        choiceGroup = null;
        answerField = null;
        submitted = false;
        Question q = questions.get(index);

        // Top bar
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");
        Label user = new Label(AppState.student != null ? AppState.student.getName() : "");
        user.getStyleClass().add("user-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(logo, spacer, user);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        // Progress
        Label progressText = new Label("QUESTION " + (index + 1) + " OF " + questions.size());
        progressText.getStyleClass().add("card-label");
        ProgressBar bar = new ProgressBar(index / (double) questions.size());
        bar.setMaxWidth(Double.MAX_VALUE);

        Label topic = new Label(q.getCourse() + "  •  " + q.getTopic() + "  •  Level " + q.getDifficulty());
        topic.getStyleClass().add("mode-desc");

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

        VBox inner = new VBox(14, text, answerArea, feedback);
        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Buttons
        Button quit = new Button("Quit");
        quit.setOnAction(e -> confirmQuit());

        Button action = new Button("Submit");
        action.setId("actionButton");
        action.getStyleClass().add("start-button");

        action.setOnAction(e -> {
            if (!submitted) {
                String answer = readAnswer();
                if (answer.isBlank()) {
                    setFeedback(feedback, "wrong", "Please enter or choose an answer first.");
                    return;
                }
                submitted = true;
                answerArea.getChildren().forEach(n -> n.setDisable(true));

                if (q.checkAnswer(answer)) {
                    correct++;
                    setFeedback(feedback, "correct", "Correct!");
                } else {
                    missed.add(q);
                    setFeedback(feedback, "wrong", "Correct answer: " + describeAnswer(q));
                }
                action.setText(index + 1 < questions.size() ? "Next" : "Finish");
            } else if (index + 1 < questions.size()) {
                index++;
                SceneManager.show(build());
            } else {
                int seconds = (int) ((System.currentTimeMillis() - startMillis) / 1000);
                onFinish.accept(new Result(questions.size(), correct, seconds, missed));
            }
        });

        // Enter in the text box = Submit
        if (answerField != null) {
            answerField.setOnAction(e -> action.fire());
        }

        Region bs = new Region();
        HBox.setHgrow(bs, Priority.ALWAYS);
        HBox buttons = new HBox(15, quit, bs, action);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, progressText, bar, topic, card, buttons);
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

    // Stores answer
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


    private static String clean(String s) {
        return s.replace("\\Delta", "Δ").replace("\\pi", "π").replace("$", "");
    }

    private void setFeedback(Label label, String styleClass, String message) {
        label.getStyleClass().setAll("label", "quiz-feedback", styleClass);
        label.setText(message);
    }

    private void confirmQuit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Quit this quiz? Your progress in this session will be lost.",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait()
                .filter(b -> b == ButtonType.YES)
                .ifPresent(b -> SceneManager.show(new MainMenu().build()));
    }
}