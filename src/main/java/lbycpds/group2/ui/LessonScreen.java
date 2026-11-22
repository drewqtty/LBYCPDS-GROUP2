package lbycpds.group2.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.logic.LessonSession;
import lbycpds.group2.logic.TutorQuestionList;
import lbycpds.group2.model.Lesson;
import lbycpds.group2.model.LessonPage;
import lbycpds.group2.model.PageBlock;
import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;

// Tutor Mode, steps 3 and 4: the student reads a lesson one page at a time, and after the last page
// chooses between a practice quiz and going back. Page-by-page rules are in LessonSession.
public class LessonScreen {

    private final String course;
    private final Lesson lesson;
    private final Question[] questions;
    private final Runnable backToTopics;
    private final LessonSession session;

    private boolean finished = false;            // true once the student pressed Finish learning
    private QuizScreen.Result lastResult;        // the last practice quiz, shown on the "what next" screen

    // backToTopics returns to the topic list (Back, and Back to Menu after the lesson)
    public LessonScreen(String course, Lesson lesson, Question[] questions, Runnable backToTopics) {
        this.course = course;
        this.lesson = lesson;
        this.questions = questions;
        this.backToTopics = backToTopics;
        this.session = new LessonSession(lesson);
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setTop(TutorTopicScreen.topBar(course));
        root.setCenter(finished ? buildNextSteps() : buildPage());
        root.getStyleClass().add("green");
        return root;
    }

    // ---- Step 3: one page of the lesson ----

    private Node buildPage() {
        Button back = new Button("Back to topics");
        back.setOnAction(e -> backToTopics.run());

        // A lesson with no pages: say so instead of showing an empty screen
        if (!session.hasContent()) {
            VBox empty = new VBox(14,
                    TutorTopicScreen.wrapped(lesson.getTitle(), "section-title"),
                    TutorTopicScreen.wrapped("This lesson has no content yet.", "quiz-question"),
                    back);
            empty.setPadding(new Insets(20, 40, 24, 40));
            return empty;
        }

        LessonPage page = session.currentPage();

        // Title, page indicator, progress bar
        Label title = TutorTopicScreen.wrapped(lesson.getTitle(), "section-title");
        Label category = new Label(lesson.getCategory());
        category.getStyleClass().add("mode-desc");
        VBox titleBox = new VBox(2, title, category);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label position = new Label("PAGE " + session.getPageNumber() + " OF " + session.getPageCount());
        position.getStyleClass().add("card-label");
        HBox titleRow = new HBox(12, titleBox, position);
        titleRow.setAlignment(Pos.BOTTOM_LEFT);

        ProgressBar progress = new ProgressBar(session.progress());
        progress.setMaxWidth(Double.MAX_VALUE);

        // The page itself, in a scroll pane so long pages never run off the screen
        VBox inner = new VBox(12);
        inner.getChildren().add(TutorTopicScreen.wrapped(page.getTitle(), "quiz-question"));
        inner.getChildren().addAll(renderBlocks(page.getBlocks()));
        ScrollPane scroll = new ScrollPane(inner);
        scroll.setFitToWidth(true);
        VBox card = new VBox(scroll);
        card.getStyleClass().add("card");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Previous / Next (Finish learning on the last page)
        Button previous = new Button("Previous page");
        previous.setDisable(!session.hasPrevious());
        previous.setOnAction(e -> {
            if (session.previous()) {
                SceneManager.show(build());
            }
        });

        boolean lastPage = session.isLastPage();
        Button next = new Button(lastPage ? "Finish learning" : "Next page");
        next.getStyleClass().add("start-button");
        next.setOnAction(e -> {
            if (lastPage) {
                finished = true;
                SceneManager.show(build());
            } else if (session.next()) {
                SceneManager.show(build());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox nav = new HBox(12, back, spacer, previous, next);
        nav.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, titleRow, progress, card, nav);
        content.setPadding(new Insets(20, 40, 24, 40));
        return content;
    }

    // Turns the parsed page into JavaFX nodes
    private static List<Node> renderBlocks(List<PageBlock> blocks) {
        List<Node> nodes = new ArrayList<>();
        for (PageBlock block : blocks) {
            switch (block.getType()) {
                case HEADING -> nodes.add(TutorTopicScreen.wrapped(block.getText(), "lesson-heading"));
                case PARAGRAPH -> nodes.add(TutorTopicScreen.wrapped(block.getText(), "lesson-text"));
                case BULLET -> nodes.add(listRow("•", block.getText()));
                case NUMBERED -> nodes.add(listRow(block.getLabel(), block.getText()));
                case CODE -> nodes.add(codeBlock(block));
            }
        }
        return nodes;
    }

    // A bullet or numbered step: a marker on the left, wrapping text on the right
    private static Node listRow(String marker, String text) {
        Label markerLabel = new Label(marker);
        markerLabel.getStyleClass().add("lesson-marker");
        markerLabel.setMinWidth(26);

        Label textLabel = TutorTopicScreen.wrapped(text, "lesson-text");
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        HBox row = new HBox(8, markerLabel, textLabel);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(0, 0, 0, 8));
        return row;
    }

    // Java code, a trace, or a diagram: monospaced, never wrapped, so the alignment stays intact
    private static Node codeBlock(PageBlock block) {
        Label code = new Label(block.getText());
        code.getStyleClass().add("lesson-code");
        code.setTextOverrun(OverrunStyle.CLIP);
        code.setMinWidth(Region.USE_PREF_SIZE);
        code.setMinHeight(Region.USE_PREF_SIZE);

        VBox box = new VBox(6);
        if ("java".equalsIgnoreCase(block.getLabel())) {
            Label caption = new Label("JAVA");
            caption.getStyleClass().add("card-label");
            box.getChildren().add(caption);
        }
        box.getChildren().add(code);
        box.getStyleClass().add("lesson-code-box");
        return box;
    }

    // ---- Step 4: what to do after the lesson ----

    private Node buildNextSteps() {
        List<Question> practice = TutorQuestionList.forLesson(lesson, questions);

        Label done = TutorTopicScreen.wrapped("Lesson complete!", "section-title");
        Label name = TutorTopicScreen.wrapped(lesson.getTitle(), "quiz-question");
        Label ask = TutorTopicScreen.wrapped("What would you like to do next?", "quiz-feedback");

        // The student chooses: nothing here starts a quiz by itself
        Button quiz = new Button("Practice Quiz");
        quiz.getStyleClass().add("start-button");
        Label note;
        if (practice.isEmpty()) {
            quiz.setDisable(true);
            note = TutorTopicScreen.wrapped("There are no practice questions for this topic yet.", "mode-desc");
        } else {
            quiz.setOnAction(e -> startPractice(practice));
            note = TutorTopicScreen.wrapped(practice.size() + " questions on " + lesson.getPracticeTopic()
                    + ", from easy to hard.", "mode-desc");
        }

        Button menu = new Button("Back to Menu");
        menu.setOnAction(e -> backToTopics.run());

        VBox inner = new VBox(14, done, name, ask);
        if (lastResult != null) {
            inner.getChildren().add(TutorTopicScreen.wrapped("Your last practice quiz: " + lastResult.correct
                    + " of " + lastResult.total + " correct (" + lastResult.percent() + "%).", "quiz-feedback"));
        }
        inner.getChildren().add(new HBox(12, quiz, menu));
        inner.getChildren().add(note);

        VBox card = new VBox(inner);
        card.getStyleClass().add("card");
        VBox.setVgrow(card, Priority.ALWAYS);

        VBox content = new VBox(10, card);
        content.setPadding(new Insets(20, 40, 24, 40));
        return content;
    }

    // Opens the existing quiz screen with the questions of this lesson's topic. Nothing is saved to the
    // student's progress: it is practice. When it ends, the student lands back on the "what next" screen.
    private void startPractice(List<Question> practice) {
        SceneManager.show(new QuizScreen(practice, result -> {
            lastResult = result;
            SceneManager.show(build());
        }).build());
    }
}