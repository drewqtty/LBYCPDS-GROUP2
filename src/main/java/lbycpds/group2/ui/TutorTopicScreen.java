package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.logic.LessonRepository;
import lbycpds.group2.logic.TutorQuestionList;
import lbycpds.group2.model.Lesson;
import lbycpds.group2.model.Question;

// Tutor Mode, step 2: the student chooses a topic to learn.
// The topics are the lessons of the course, grouped by category (see LessonRepository).
// Pressing Begin learning opens the lesson (LessonScreen).
public class TutorTopicScreen {

    private final String course;
    private final LessonRepository lessons;
    private final Question[] questions;
    private Lesson selected;   // the topic that starts out chosen (the one the student just studied)

    public TutorTopicScreen(String course, LessonRepository lessons, Question[] questions) {
        this.course = course;
        this.lessons = lessons;
        this.questions = questions;
    }

    // Opens with this topic already chosen. Used when coming back from a lesson.
    public Parent build(Lesson chosen) {
        this.selected = chosen;
        return build();
    }

    public Parent build() {
        Label title = new Label("What would you like to learn?");
        title.getStyleClass().add("section-title");

        Button menu = new Button("Back to menu");
        menu.setOnAction(e -> SceneManager.show(new MainMenu().build()));

        // The earlier Tutor Mode: try questions with answer keys, no lesson first
        Button practice = new Button("Answer-key practice");
        practice.setOnAction(e -> SceneManager.show(new TutorScreen(course, questions).build()));

        if (lessons.isEmpty()) {
            return emptyView(title, menu);
        }

        Label hint = new Label("Choose a topic, then press Begin learning. When you finish a lesson you can "
                + "choose to take a practice quiz on it.");
        hint.getStyleClass().add("mode-desc");
        hint.setWrapText(true);
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setMinHeight(Region.USE_PREF_SIZE);

        // One card per topic, grouped under its category
        ToggleGroup group = new ToggleGroup();
        VBox list = new VBox(10);
        for (String category : lessons.getCategories()) {
            Label categoryLabel = new Label(category.toUpperCase());
            categoryLabel.getStyleClass().add("card-label");
            list.getChildren().add(categoryLabel);

            for (Lesson lesson : lessons.getLessonsIn(category)) {
                RadioButton radio = new RadioButton();
                radio.setToggleGroup(group);
                radio.setUserData(lesson);
                list.getChildren().add(MainMenu.modeCard(lesson.getTitle(), describe(lesson), radio));
                if (lesson == selected) {
                    radio.setSelected(true);
                }
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        Button begin = new Button("BEGIN LEARNING  ");
        begin.getStyleClass().add("start-button");
        begin.setOnAction(e -> {
            Toggle chosen = group.getSelectedToggle();
            if (chosen == null) {
                Dialogs.tell("Please choose a topic first.");
                return;
            }
            open((Lesson) chosen.getUserData());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttons = new HBox(12, menu, practice, spacer, begin);
        buttons.setAlignment(Pos.CENTER_LEFT);

        return page(new VBox(10, title, hint, card, buttons));
    }

    // Shown when the course has no lessons yet
    private Parent emptyView(Label title, Button menu) {
        Label none = wrapped("There are no lessons for " + course + " yet.", "quiz-question");
        Label why = wrapped("You can still try the practice questions for this course.", "mode-desc");

        Button practice = new Button("PRACTICE QUESTIONS");
        practice.getStyleClass().add("start-button");
        practice.setOnAction(e -> SceneManager.show(new TutorScreen(course, questions).build()));

        return page(new VBox(14, title, none, why, new HBox(12, menu, practice)));
    }

    // Opens a lesson. Its Back and Back to Menu buttons return to this screen with the topic still chosen.
    private void open(Lesson lesson) {
        SceneManager.show(new LessonScreen(course, lesson, questions,
                () -> SceneManager.show(build(lesson))).build());
    }

    // "5 pages • Practice quiz: 9 questions"
    private String describe(Lesson lesson) {
        int pages = lesson.getPageCount();
        String length = pages == 0 ? "No content yet" : pages + (pages == 1 ? " page" : " pages");
        int count = TutorQuestionList.forLesson(lesson, questions).size();
        String practice = count == 0 ? "No practice questions yet" : "Practice quiz: " + count + " questions";
        return length + "   •   " + practice;
    }

    // ---- Pieces shared with LessonScreen ----

    // Top bar used by every Tutor Mode screen
    static HBox topBar(String course) {
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");
        Label mode = new Label("Tutor Mode  |  " + course);
        mode.getStyleClass().add("user-name");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox bar = new HBox(16, logo, spacer, mode);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("topbar");
        return bar;
    }

    // A label that wraps its text and is never cut off
    static Label wrapped(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private Parent page(Node content) {
        VBox box = content instanceof VBox ? (VBox) content : new VBox(content);
        box.setPadding(new Insets(20, 40, 24, 40));
        BorderPane root = new BorderPane();
        root.setTop(topBar(course));
        root.setCenter(box);
        root.getStyleClass().add("green");
        return root;
    }
}