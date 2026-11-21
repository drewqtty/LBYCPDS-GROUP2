package lbycpds.group2.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.util.Duration;
import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
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
    private final BiConsumer<Question, String> onAnswer;   // may be null

    private int index = 0;
    private int correct = 0;
    private final List<Question> missed = new ArrayList<>();
    private final long startMillis = System.currentTimeMillis();

    private ToggleGroup choiceGroup;
    private TextField answerField;
    private boolean submitted;

    // Time limit for the whole exam, counted down once and kept across questions
    private final int timeLimitSeconds;
    private int secondsLeft;
    private Timeline clock;
    private Label timerLabel;
    private boolean finished;

    public QuizScreen(List<Question> questions, Consumer<Result> onFinish) {
        this(questions, null, onFinish);
    }

    public QuizScreen(List<Question> questions, BiConsumer<Question, String> onAnswer, Consumer<Result> onFinish) {
        this.questions = questions;
        this.onAnswer = onAnswer;
        this.onFinish = onFinish;

        int total = 0;
        for (Question q : questions) {
            total += secondsFor(q);
        }
        this.timeLimitSeconds = total;
        this.secondsLeft = total;
    }

    // Time given for one question: multiple choice 30s, identification 45s, problem solving 120s
    private static int secondsFor(Question q) {
        if (q instanceof MultipleChoiceQuestion) {
            return 30;
        }
        if ("PROBLEM_SOLVING".equals(q.getType())) {
            return 120;
        }
        return 45;
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
        timerLabel = new Label();
        timerLabel.getStyleClass().add("timer-label");
        showTime();
        HBox.setMargin(timerLabel, new Insets(0, 24, 0, 0));
        HBox topBar = new HBox(logo, spacer, timerLabel, user);
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

                if (record(q, answer)) {
                    setFeedback(feedback, "correct", "Correct!");
                } else {
                    setFeedback(feedback, "wrong", "Correct answer: " + describeAnswer(q));
                }
                action.setText(index + 1 < questions.size() ? "Next" : "Finish");
            } else if (index + 1 < questions.size()) {
                index++;
                SceneManager.show(build());
            } else {
                stopClock();
                int seconds = (int) ((System.currentTimeMillis() - startMillis) / 1000);
                onFinish.accept(new Result(questions.size(), correct, seconds, missed));
            }
        });

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

        if (clock == null) {
            startClock();
        }
        return root;
    }

    // Sends the answer to the session and counts it. Returns true if it was correct.
    private boolean record(Question q, String answer) {
        if (onAnswer != null) {
            onAnswer.accept(q, answer);
        }
        if (q.checkAnswer(answer)) {
            correct++;
            return true;
        }
        missed.add(q);
        return false;
    }

    private void startClock() {
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void stopClock() {
        finished = true;
        if (clock != null) {
            clock.stop();
        }
    }

    private void tick() {
        if (finished) {
            return;
        }
        secondsLeft--;
        showTime();
        if (secondsLeft <= 0) {
            clock.stop();
            // Dialogs cannot open while the timer is running its pulse, so do it right after
            Platform.runLater(this::timeUp);
        }
    }

    private void showTime() {
        int shown = Math.max(0, secondsLeft);
        timerLabel.setText(String.format("%02d:%02d", shown / 60, shown % 60));
        timerLabel.getStyleClass().remove("timer-low");
        if (shown <= 60) {
            timerLabel.getStyleClass().add("timer-low");
        }
    }

    // Time ran out (a typed answer still counts)
    private void timeUp() {
        if (finished) {
            return;
        }
        finished = true;

        int next = index + 1;
        if (!submitted) {
            record(questions.get(index), readAnswer());
        }
        for (int i = next; i < questions.size(); i++) {
            record(questions.get(i), "");
        }

        Dialogs.tell("Time's up! The questions you did not answer are marked wrong.");
        onFinish.accept(new Result(questions.size(), correct, timeLimitSeconds, missed));
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
        clock.pause();
        int answer = Dialogs.ask("Quit this quiz? Your progress in this session will be lost.",
                "Yes", "No");
        if (answer == 0) {
            stopClock();
            SceneManager.show(new MainMenu().build());
        } else if (!finished) {
            clock.play();
        }
    }
}