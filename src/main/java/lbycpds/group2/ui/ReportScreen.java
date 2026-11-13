package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.logic.InsertionSort;
import lbycpds.group2.logic.TextReportGenerator;
import lbycpds.group2.model.Session;
import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.List;

/**
 * Progress report. Pass the data in so it works with fake data now and the real
 * stores/controller later:
 *   new ReportScreen(AppState.topics, sessionsFromStore).build()
 */
public class ReportScreen {

    private final List<Topic> topics;
    private final List<Session> sessions;

    public ReportScreen(List<Topic> topics, List<Session> sessions) {
        this.topics = new ArrayList<>(topics);
        this.sessions = sessions;
        InsertionSort.sort(this.topics);          // weakest first
    }

    public Parent build() {
        // ----- Top bar (same as other screens) -----
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");
        Label user = new Label(AppState.student != null ? AppState.student.getName() : "");
        user.getStyleClass().add("user-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(logo, spacer, user);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        Label title = new Label("Your Progress Report");
        title.getStyleClass().add("section-title");

        // ----- Summary cards -----
        String weakest = topics.isEmpty() ? "-" : topics.get(0).getName();
        HBox stats = new HBox(14,
                statCard("SESSIONS", String.valueOf(sessions.size())),
                statCard("TOTAL TIME", TextReportGenerator.formatTime(TextReportGenerator.totalSeconds(sessions))),
                statCard("AVERAGE SCORE", TextReportGenerator.averageScore(sessions) + "%"),
                statCard("FOCUS NEXT ON", weakest));
        for (var n : stats.getChildren()) HBox.setHgrow(n, Priority.ALWAYS);

        // ----- Topic rows, weakest first -----
        Label header = new Label("TOPICS  (WEAKEST FIRST)");
        header.getStyleClass().add("card-label");

        VBox rows = new VBox(8, header);
        int rank = 1;
        for (Topic t : topics) rows.getChildren().add(topicRow(rank++, t));

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // ----- Buttons -----
        Button menu = new Button("Main Menu");
        menu.setOnAction(e -> SceneManager.show(new MainMenu().build()));

        Button review = new Button("Review Missed Questions");
        review.setOnAction(e -> {
            // TODO: SceneManager.show(new ReviewSheetScreen(missedQuestions).build());
        });

        Region bs = new Region();
        HBox.setHgrow(bs, Priority.ALWAYS);
        HBox buttons = new HBox(15, menu, bs, review);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, title, stats, card, buttons);
        content.setPadding(new Insets(24, 40, 24, 40));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(content);
        root.getStyleClass().add("green");
        return root;
    }

    private VBox statCard(String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("card-label");
        Label v = new Label(value);
        v.getStyleClass().add("stat-value");
        v.setWrapText(true);
        VBox box = new VBox(4, l, v);
        box.getStyleClass().add("card");
        box.setPrefWidth(1);
        return box;
    }

    private HBox topicRow(int rank, Topic t) {
        Label num = new Label(String.valueOf(rank));
        num.getStyleClass().add("rank-number");
        num.setMinWidth(24);

        Label name = new Label(t.getName());
        name.getStyleClass().add("topic-name");
        name.setPrefWidth(210);
        name.setMinWidth(210);

        Label conf = new Label("Confidence " + t.getConfidence() + "/5");
        conf.getStyleClass().add("rating-badge");
        conf.setPrefWidth(130);
        conf.setMinWidth(130);
        conf.setAlignment(Pos.CENTER);

        Label level = new Label("Level " + t.getLevel());
        level.getStyleClass().add("mode-desc");
        level.setPrefWidth(60);
        level.setMinWidth(60);

        ProgressBar bar = new ProgressBar(t.getLatestScore() / 100.0);
        bar.setPrefWidth(160);
        bar.setMinWidth(100);
        HBox.setHgrow(bar, Priority.ALWAYS);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label scores = new Label("Latest " + t.getLatestScore() + "%   Best " + t.getBestScore() + "%");
        scores.getStyleClass().add("mode-desc");
        scores.setPrefWidth(150);
        scores.setMinWidth(150);

        HBox row = new HBox(14, num, name, conf, level, bar, scores);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("topic-row");
        return row;
    }
}