package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class ConfidenceScreen {

    private static final String[] RATING_WORDS = {
            "", "Not confident", "Shaky", "Okay", "Confident", "Very confident"
    };

    List<Slider> sliders = new ArrayList<>();

    public Parent build() {
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

        // Title
        Label title = new Label("Rate Your Confidence");
        title.getStyleClass().add("section-title");

        String modeText = "CRAM".equals(AppState.mode)
                ? "Cram mode  •  all courses"
                : "Regular mode  •  " + AppState.course;
        Label context = new Label(modeText + "   |   1 = not confident, 5 = very confident. "
                + "Topics you rate lowest are quizzed first.");
        context.getStyleClass().add("card-label");
        context.setWrapText(true);

        // One row per topic
        VBox rows = new VBox(10);
        rows.setFillWidth(true);

        String lastCourse = null;
        for (Topic topic : AppState.topics) {
            if ("CRAM".equals(AppState.mode) && !topic.getCourse().equals(lastCourse)) {
                Label courseHeader = new Label(topic.getCourse().toUpperCase());
                courseHeader.getStyleClass().add("card-label");
                courseHeader.setPadding(new Insets(lastCourse == null ? 0 : 12, 0, 2, 0));
                rows.getChildren().add(courseHeader);
                lastCourse = topic.getCourse();
            }

            Label name = new Label(topic.getName());
            name.getStyleClass().add("topic-name");
            name.setPrefWidth(240);
            name.setMinWidth(240);

            Slider slider = new Slider(1, 5, 3);
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            slider.setShowTickMarks(true);
            slider.setShowTickLabels(false);
            HBox.setHgrow(slider, Priority.ALWAYS);

            // Show the saved rating
            if (topic.getConfidence() > 0) {
                slider.setValue(topic.getConfidence());
            }

            Label badge = new Label();
            badge.getStyleClass().add("rating-badge");
            badge.setPrefWidth(150);
            badge.setMinWidth(150);
            badge.setAlignment(Pos.CENTER);
            updateBadge(badge, (int) Math.round(slider.getValue()));
            slider.valueProperty().addListener((obs, was, is) ->
                    updateBadge(badge, (int) Math.round(is.doubleValue())));

            sliders.add(slider);

            HBox row = new HBox(16, name, slider, badge);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("topic-row");
            rows.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Buttons
        Button back = new Button("Back");
        back.setOnAction(e -> SceneManager.show(new MainMenu().build()));

        Button next = new Button("Start Quiz");
        next.getStyleClass().add("start-button");
        next.setOnAction(e -> {
            for (int i = 0; i < AppState.topics.size(); i++) {
                int rating = (int) Math.round(sliders.get(i).getValue());
                AppState.topics.get(i).setConfidence(rating);
                // TODO: topicStore.saveTopic(AppState.student.getName(), AppState.topics.get(i));
            }
            startQuiz();
        });

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        HBox buttons = new HBox(15, back, buttonSpacer, next);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, title, context, card, buttons);
        content.setPadding(new Insets(24, 40, 24, 40));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(content);
        root.getStyleClass().add("green");
        return root;
    }

    private void startQuiz() {
        // TODO: open the quiz
    }

    private void updateBadge(Label badge, int rating) {
        badge.setText(rating + "  -  " + RATING_WORDS[rating]);
    }
}