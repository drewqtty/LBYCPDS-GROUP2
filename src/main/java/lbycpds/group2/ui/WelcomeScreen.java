package lbycpds.group2.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lbycpds.group2.database.StudentStore;
import lbycpds.group2.model.Student;

public class WelcomeScreen {

    // TODO: replace with Drew's real implementation once it's in main
    StudentStore studentStore = null;

    public Parent build() {
        Label title = new Label("Welcome to Cram!");
        title.getStyleClass().add("heading");

        Label prompt = new Label("Enter your name to begin");

        TextField nameField = new TextField();
        nameField.setPromptText("Your name");
        nameField.setMaxWidth(300);

        Label error = new Label();
        error.getStyleClass().add("wrong");

        Button next = new Button("Next");
        next.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                error.setText("Please enter your name.");
                return;
            }

            // Look for saved data (skipped until the database is ready)
            Student existing = null;
            if (studentStore != null) {
                existing = studentStore.loadStudent(name);
            }

            if (existing != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "A profile named " + name + " already exists. Is this you?",
                        ButtonType.YES, ButtonType.NO);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.NO) {
                    error.setText("Please enter a different name.");
                    return;
                }
                AppState.student = existing;
                AppState.returning = true;
            } else {
                AppState.student = new Student(name);
                AppState.returning = false;
            }

            SceneManager.show(new MainMenu().build());
        });

        // Pressing Enter also works
        nameField.setOnAction(e -> next.fire());

        VBox root = new VBox(15, title, prompt, nameField, next, error);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("green");
        return root;


    }
}