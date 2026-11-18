package lbycpds.group2.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


public class Dialogs {

    public static int ask(String message, String... buttons) {
        Scene scene = SceneManager.scene;
        Parent page = scene.getRoot();
        int[] choice = {0};
        Object key = new Object();

        Label text = new Label(message);
        text.setWrapText(true);
        text.setMaxWidth(440);
        text.setStyle("-fx-font-size: 16px; -fx-text-fill: #1d1d1d;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            Button button = new Button(buttons[i]);
            button.setStyle("-fx-font-size: 14px; -fx-padding: 8 22 8 22;");
            button.setDefaultButton(i == 0);
            button.setOnAction(e -> {
                choice[0] = index;
                Platform.exitNestedEventLoop(key, null);
            });
            row.getChildren().add(button);
        }

        VBox box = new VBox(22, text, row);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(28, 34, 28, 34));
        box.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 14;");

        StackPane overlay = new StackPane(box);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.55);");

        StackPane holder = new StackPane();
        scene.setRoot(holder);
        page.setDisable(true);
        holder.getChildren().addAll(page, overlay);
        row.getChildren().get(0).requestFocus();

        Platform.enterNestedEventLoop(key);

        holder.getChildren().clear();
        page.setDisable(false);
        scene.setRoot(page);
        return choice[0];
    }

    public static void tell(String message) {
        ask(message, "OK");
    }
}