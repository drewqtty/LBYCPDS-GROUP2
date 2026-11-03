package lbycpds.group2.ui;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen extends Application {

    // Window size
    double WIDTH = 800;
    double HEIGHT = 500;

    @Override
    public void start(Stage stage) {
        // Application name
        Label title = new Label("Cram");
        title.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: #1F5C3A;");
        Label subtitle = new Label("Comprehensive Review and Adaptive Mastery Program");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #595959;");

        VBox splashRoot = new VBox(16, title, subtitle);
        splashRoot.setAlignment(Pos.CENTER); // Centers everything
        splashRoot.setPadding(new Insets(20));
        splashRoot.setStyle("-fx-background-color: #F5F3E8;");

        Scene splashScene = new Scene(splashRoot, WIDTH, HEIGHT);

        stage.setTitle("Cram");
        stage.setScene(splashScene);
        stage.show();

        // Transition pause before going to the next scene
        PauseTransition wait = new PauseTransition(Duration.seconds(2.5));

        wait.setOnFinished(event -> stage.setScene(buildNextScene()));
        wait.play();
    }

    // Go to a new frame
    public Scene buildNextScene() {
        Label placeholder = new Label("Placeholder to next scene");
        placeholder.setStyle("-fx-font-size: 24px;");

        StackPane root = new StackPane(placeholder);
        return new Scene(root, WIDTH, HEIGHT);
    }
}

