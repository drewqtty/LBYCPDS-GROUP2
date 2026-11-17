package lbycpds.group2.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// First screen after the splash: lets the user pick Admin or Student
public class RoleScreen {

    public Parent build() {
        Label title = new Label("Welcome to Cram!");
        title.getStyleClass().add("heading");

        Label prompt = new Label("Who's using Cram?");

        Button student = new Button("Student");
        student.setOnAction(e -> choose(AppState.Role.STUDENT));

        Button admin = new Button("Admin");
        admin.setOnAction(e -> choose(AppState.Role.ADMIN));

        HBox buttons = new HBox(15, student, admin);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, title, prompt, buttons);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("green");
        return root;
    }

    private void choose(AppState.Role role) {
        AppState.role = role;
        SceneManager.show(new WelcomeScreen().build());
    }
}