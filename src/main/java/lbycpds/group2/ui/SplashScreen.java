package lbycpds.group2.ui;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreen extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Cram");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Comprehensive Review and Adaptive Mastery Program");
        subtitle.getStyleClass().add("subtitle");

        VBox splashRoot = new VBox(16, title, subtitle);
        splashRoot.setAlignment(Pos.CENTER);
        splashRoot.setPadding(new Insets(20));
        splashRoot.getStyleClass().add("green");

        stage.setTitle("Cram");
        SceneManager.init(stage);
        SceneManager.show(splashRoot);
        stage.show();

        PauseTransition wait = new PauseTransition(Duration.seconds(2.5));
        wait.setOnFinished(event -> SceneManager.show(new WelcomeScreen().build()));
        wait.play();
    }
}