package lbycpds.group2.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lbycpds.group2.logic.QuestionBankEditor;
import lbycpds.group2.logic.QuestionBankFiles;
import lbycpds.group2.logic.TextFileQuestionBank;
import lbycpds.group2.model.Question;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class AdminScreen {

    private static final String[] BANK_RESOURCES = {"/questions/circuits.txt", "/questions/dsa.txt"};
    private static final String[] TYPE_CODES = {"MC", "ID", "PS"};
    private static final String[] TYPE_NAMES = {"Multiple choice", "Identification", "Problem solving"};
    private static final String ALL_TOPICS = "All topics";

    private final List<String> courses = new ArrayList<>();
    private final List<Path> files = new ArrayList<>();

    private QuestionBankEditor editor;   // for the selected course
    private Question editing;
    private boolean refreshing;

    // Left side
    private final ObservableList<Question> rows = FXCollections.observableArrayList();
    private final TableView<Question> table = new TableView<>(rows);
    private final ComboBox<String> courseBox = new ComboBox<>();
    private final ComboBox<String> topicFilter = new ComboBox<>();

    // Right side
    private final Label formTitle = new Label();
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final ComboBox<String> topicBox = new ComboBox<>();
    private final ComboBox<Integer> levelBox = new ComboBox<>();
    private final TextArea questionArea = new TextArea();
    private final TextField[] choiceFields = new TextField[4];
    private final ComboBox<String> answerLetterBox = new ComboBox<>();
    private final TextField idAnswerField = new TextField();
    private final TextArea psAnswerArea = new TextArea();
    private final TextField keywordsField = new TextField();
    private final VBox mcBox = new VBox(10);
    private final VBox idBox = new VBox(10);
    private final VBox psBox = new VBox(10);
    private final Button saveButton = new Button();
    private final Label message = new Label();

    public Parent build() {
        try {
            for (String resource : BANK_RESOURCES) {
                Path file = QuestionBankFiles.fileFor(resource);
                courses.add(new TextFileQuestionBank(file).getCourse());
                files.add(file);
            }
        } catch (IOException | RuntimeException e) {
            return errorScreen("Could not open the question files: " + e.getMessage());
        }

        // Top bar
        Label logo = new Label("Cram");
        logo.getStyleClass().add("app-name");
        String adminName = AppState.adminName != null ? AppState.adminName : "";
        Label role = new Label("Admin  |  " + adminName + "  |  Question Editor");
        role.getStyleClass().add("user-name");
        Button logout = new Button("Log out");
        logout.getStyleClass().add("small-button");
        logout.setOnAction(e -> SceneManager.show(new RoleScreen().build()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(16, logo, spacer, role, logout);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        // Left
        Label listHeader = new Label("QUESTION BANK");
        listHeader.getStyleClass().add("card-label");

        courseBox.getItems().addAll(courses);
        courseBox.setValue(courses.get(0));
        courseBox.setMaxWidth(Double.MAX_VALUE);
        topicFilter.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(courseBox, Priority.ALWAYS);
        HBox.setHgrow(topicFilter, Priority.ALWAYS);
        HBox filters = new HBox(10, courseBox, topicFilter);

        buildTable();

        Button addNew = new Button("+ New question");
        addNew.setOnAction(e -> newQuestion());

        VBox leftCard = new VBox(12, listHeader, filters, table, addNew);
        leftCard.getStyleClass().add("card");
        VBox.setVgrow(table, Priority.ALWAYS);

        // Right
        ScrollPane rightScroll = new ScrollPane(buildForm());
        rightScroll.setFitToWidth(true);

        HBox columns = new HBox(24, leftCard, rightScroll);
        HBox.setHgrow(leftCard, Priority.ALWAYS);
        HBox.setHgrow(rightScroll, Priority.ALWAYS);
        leftCard.setPrefWidth(520);
        rightScroll.setPrefWidth(440);
        columns.setPadding(new Insets(20, 30, 20, 30));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(columns);
        root.getStyleClass().add("green");


        courseBox.setOnAction(e -> {
            if (!refreshing) {
                openCourse();
            }
        });
        topicFilter.setOnAction(e -> {
            if (!refreshing) {
                reloadTable();
            }
        });
        table.getSelectionModel().selectedItemProperty().addListener((obs, was, q) -> {
            if (!refreshing && q != null) {
                loadIntoForm(q);
            }
        });

        openCourse();
        return root;
    }

    // Table

    private void buildTable() {
        TableColumn<Question, String> idCol = column("ID", q -> String.valueOf(q.getId()), 50);
        TableColumn<Question, String> topicCol = column("Topic", Question::getTopic, 130);
        TableColumn<Question, String> levelCol = column("Lv", q -> String.valueOf(q.getDifficulty()), 40);
        TableColumn<Question, String> typeCol = column("Type", QuestionBankEditor::typeCode, 50);
        TableColumn<Question, String> textCol = column("Question", Question::getText, 260);
        table.getColumns().add(idCol);
        table.getColumns().add(topicCol);
        table.getColumns().add(levelCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(textCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No questions to show."));
    }

    private TableColumn<Question, String> column(String title, Function<Question, String> value, double width) {
        TableColumn<Question, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.apply(c.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    // A different course was chosen
    private void openCourse() {
        int index = courseBox.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            return;
        }
        editor = new QuestionBankEditor(files.get(index));
        try {
            List<String> topics = editor.topics();
            List<String> filterItems = new ArrayList<>();
            filterItems.add(ALL_TOPICS);
            filterItems.addAll(topics);

            refreshing = true;
            topicFilter.getItems().setAll(filterItems);
            topicFilter.setValue(ALL_TOPICS);
            topicBox.getItems().setAll(topics);
            refreshing = false;
        } catch (RuntimeException e) {
            refreshing = false;
            showMessage("Could not read the question file: " + e.getMessage(), false);
            return;
        }
        newQuestion();
        reloadTable();
    }

    // Fills the table from the file, showing only the chosen topic
    private void reloadTable() {
        String filter = topicFilter.getValue();
        try {
            Question[] all = editor.load().loadQuestions();
            refreshing = true;
            rows.clear();
            for (Question q : all) {
                if (filter == null || filter.equals(ALL_TOPICS) || q.getTopic().equals(filter)) {
                    rows.add(q);
                }
            }
            refreshing = false;
        } catch (RuntimeException e) {
            refreshing = false;
            showMessage("Could not read the question file: " + e.getMessage(), false);
            return;
        }
        if (editing != null) {
            Question again = findRow(editing.getId());
            if (again != null) {
                selectRow(again);
            }
        }
    }

    private Question findRow(int id) {
        for (Question q : rows) {
            if (q.getId() == id) {
                return q;
            }
        }
        return null;
    }

    private void selectRow(Question q) {
        refreshing = true;
        table.getSelectionModel().select(q);
        table.scrollTo(q);
        refreshing = false;
    }

    // Form

    private Node buildForm() {
        formTitle.getStyleClass().add("card-label");

        typeBox.getItems().addAll(TYPE_NAMES);
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setOnAction(e -> showTypePane());

        topicBox.setPromptText("Choose a topic");
        topicBox.setMaxWidth(Double.MAX_VALUE);
        levelBox.getItems().addAll(1, 2, 3);
        levelBox.setPromptText("1-3");

        VBox topicColumn = labeled("TOPIC", topicBox);
        HBox.setHgrow(topicColumn, Priority.ALWAYS);
        HBox typeRow = new HBox(10, labeled("TYPE", typeBox), topicColumn, labeled("LEVEL", levelBox));

        questionArea.setWrapText(true);
        questionArea.setPrefRowCount(3);
        questionArea.setPromptText("Type the question here (do not use the # symbol)");

        // Multiple choice
        VBox choices = new VBox(6);
        for (int i = 0; i < 4; i++) {
            choiceFields[i] = new TextField();
            choiceFields[i].setPromptText("Choice " + (char) ('A' + i));
            choices.getChildren().add(choiceFields[i]);
        }
        answerLetterBox.getItems().addAll("A", "B", "C", "D");
        answerLetterBox.setPromptText("Which one is correct?");
        mcBox.getChildren().addAll(labeled("CHOICES", choices), labeled("CORRECT CHOICE", answerLetterBox));

        // Identification
        idAnswerField.setPromptText("One short answer");
        idBox.getChildren().add(labeled("CORRECT ANSWER (capital letters do not matter)", idAnswerField));

        // Problem solving
        psAnswerArea.setWrapText(true);
        psAnswerArea.setPrefRowCount(3);
        psAnswerArea.setPromptText("The worked solution shown to the student");
        keywordsField.setPromptText("e.g. 3, 27   (separate with commas)");
        psBox.getChildren().addAll(
                labeled("MODEL ANSWER", psAnswerArea),
                labeled("KEYWORDS (every one must appear in the student's answer)", keywordsField));

        saveButton.setOnAction(e -> save());
        message.setWrapText(true);
        HBox actions = new HBox(14, saveButton, message);
        actions.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(message, Priority.ALWAYS);

        VBox form = new VBox(14, formTitle, typeRow, labeled("QUESTION", questionArea),
                mcBox, idBox, psBox, actions);
        form.getStyleClass().add("card");
        return form;
    }

    private VBox labeled(String caption, Node control) {
        Label label = new Label(caption);
        label.getStyleClass().add("card-label");
        label.setWrapText(true);
        return new VBox(4, label, control);
    }

    // Shows only the answer fields that belong to the chosen question type
    private void showTypePane() {
        int type = typeBox.getSelectionModel().getSelectedIndex();
        setShown(mcBox, type == 0);
        setShown(idBox, type == 1);
        setShown(psBox, type == 2);
    }

    private void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private void clearFields() {
        topicBox.setValue(null);
        levelBox.setValue(null);
        questionArea.clear();
        for (TextField field : choiceFields) {
            field.clear();
        }
        answerLetterBox.setValue(null);
        idAnswerField.clear();
        psAnswerArea.clear();
        keywordsField.clear();
    }

    // Blank form
    private void newQuestion() {
        editing = null;
        refreshing = true;
        table.getSelectionModel().clearSelection();
        refreshing = false;

        formTitle.setText("ADD A QUESTION");
        saveButton.setText("Add question");
        clearFields();
        typeBox.getSelectionModel().select(0);
        showTypePane();

        String filter = topicFilter.getValue();
        if (filter != null && !filter.equals(ALL_TOPICS)) {
            topicBox.setValue(filter);
        }
        message.setText("");
    }


    private void loadIntoForm(Question q) {
        enterEditMode(q);
        QuestionBankEditor.Draft d = QuestionBankEditor.toDraft(q);

        clearFields();
        for (int i = 0; i < TYPE_CODES.length; i++) {
            if (TYPE_CODES[i].equals(d.type)) {
                typeBox.getSelectionModel().select(i);
            }
        }
        showTypePane();

        topicBox.setValue(d.topic);
        levelBox.setValue(d.difficulty);
        questionArea.setText(d.text);
        if (d.type.equals("MC")) {
            for (int i = 0; i < 4; i++) {
                choiceFields[i].setText(d.choices[i]);
            }
            answerLetterBox.setValue(d.answer.trim().toUpperCase());
        } else if (d.type.equals("ID")) {
            idAnswerField.setText(d.answer);
        } else {
            psAnswerArea.setText(d.answer);
            keywordsField.setText(String.join(", ", d.keywords));
        }
        message.setText("");
    }

    private void enterEditMode(Question q) {
        editing = q;
        formTitle.setText("EDIT QUESTION #" + q.getId());
        saveButton.setText("Save changes");
    }

    private QuestionBankEditor.Draft readForm() {
        QuestionBankEditor.Draft d = new QuestionBankEditor.Draft();
        int type = typeBox.getSelectionModel().getSelectedIndex();
        d.type = type < 0 ? null : TYPE_CODES[type];
        d.topic = topicBox.getValue();
        d.difficulty = levelBox.getValue() == null ? 0 : levelBox.getValue();
        d.text = questionArea.getText();
        if ("MC".equals(d.type)) {
            d.choices = new String[4];
            for (int i = 0; i < 4; i++) {
                d.choices[i] = choiceFields[i].getText();
            }
            d.answer = answerLetterBox.getValue();
        } else if ("ID".equals(d.type)) {
            d.answer = idAnswerField.getText();
        } else if ("PS".equals(d.type)) {
            d.answer = psAnswerArea.getText();
            d.keywords = keywordsField.getText().split(",");
        }
        return d;
    }

    // Saving
    private void save() {
        try {
            QuestionBankEditor.Draft d = readForm();
            String problem = editor.check(d);
            if (problem != null) {
                showMessage(problem, false);
                return;
            }

            int id;
            String done;
            if (editing == null) {
                id = editor.add(d);
                done = "Added question #" + id + ".";
            } else {
                id = editing.getId();
                editor.update(id, d);
                done = "Saved question #" + id + ".";
            }

            // Ensures the saved question is visible in the table
            String filter = topicFilter.getValue();
            if (filter != null && !filter.equals(ALL_TOPICS) && !filter.equals(d.topic.trim())) {
                refreshing = true;
                topicFilter.setValue(ALL_TOPICS);
                refreshing = false;
            }
            editing = null;
            reloadTable();
            Question saved = findRow(id);
            if (saved != null) {
                enterEditMode(saved);
                selectRow(saved);
            }
            showMessage(done, true);
        } catch (IOException | RuntimeException e) {
            showMessage("Could not save: " + e.getMessage(), false);
        }
    }

    private void showMessage(String text, boolean ok) {
        message.setText(text);
        message.getStyleClass().removeAll("correct", "wrong");
        message.getStyleClass().add(ok ? "correct" : "wrong");
    }

    private Parent errorScreen(String text) {
        Label title = new Label("Question Editor");
        title.getStyleClass().add("heading");
        Label error = new Label(text);
        error.getStyleClass().add("wrong");
        error.setWrapText(true);
        error.setMaxWidth(600);
        Button back = new Button("Back");
        back.setOnAction(e -> SceneManager.show(new RoleScreen().build()));
        VBox root = new VBox(15, title, error, back);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("green");
        return root;
    }
}