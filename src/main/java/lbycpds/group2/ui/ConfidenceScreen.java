package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class ConfidenceScreen {

    List<Slider> sliders = new ArrayList<>();

    public Parent build() {
        Label title = new Label("Rate Your Confidence");
        title.getStyleClass().add("heading");

        Label hint = new Label("1 = not confident, 5 = very confident");
        hint.getStyleClass().add("subtitle");

        // One row per topic
        VBox rows = new VBox(12);
        rows.setAlignment(Pos.CENTER);
        rows.setPadding(new Insets(10));

        for (Topic topic : AppState.topics) {
            Label name = new Label(topic.getName());
            name.setPrefWidth(250);

            Slider slider = new Slider(1, 5, 3);
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setSnapToTicks(true);
            slider.setShowTickMarks(true);
            slider.setShowTickLabels(true);
            slider.setPrefWidth(250);

            // Show the saved rating if there is one
            if (topic.getConfidence() > 0) {
                slider.setValue(topic.getConfidence());
            }

            sliders.add(slider);

            HBox row = new HBox(15, name, slider);
            row.setAlignment(Pos.CENTER);
            rows.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(320);
        scroll.setMaxWidth(650);

        Button back = new Button("Back");
        back.setOnAction(e -> SceneManager.show(new MainMenu().build()));

        Button next = new Button("Next");
        next.setOnAction(e -> {
            // Save each slider value into its topic (same order as the list)
            for (int i = 0; i < AppState.topics.size(); i++) {
                int rating = (int) sliders.get(i).getValue();
                AppState.topics.get(i).setConfidence(rating);
                // TODO: topicStore.saveTopic(AppState.student.getName(), AppState.topics.get(i));
            }

            // TODO: SceneManager.show(new QuizScreen().build());
        });

        HBox buttons = new HBox(15, back, next);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, title, hint, scroll, buttons);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("green");
        return root;
    }
}