package lbycpds.group2.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Manages every scene of the application
public class SceneManager {
    static final double WIDTH = 1000;
    static final double HEIGHT = 600;

    static Stage stage;
    static Scene scene;

    public static void init(Stage s) {
        stage = s;

        // Load fonts
        Font.loadFont(SceneManager.class.getResourceAsStream("/fonts/Poppins-Regular.ttf"), 12);
        Font.loadFont(SceneManager.class.getResourceAsStream("/fonts/Poppins-Bold.ttf"), 12);

        scene = new Scene(new StackPane(), WIDTH, HEIGHT);
        scene.getStylesheets().add(
                SceneManager.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void show(Parent root) {
        scene.setRoot(root);
    }
}