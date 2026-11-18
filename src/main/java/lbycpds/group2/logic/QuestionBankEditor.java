package lbycpds.group2.logic;

import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.ProblemSolvingQuestion;
import lbycpds.group2.model.Question;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

// Adds and edits questions in a question bank file (see docs/question-format.md).
// Only the question lines are touched: TOPIC lines, comments and blank lines are kept as they are.
// Every save is re-read with TextFileQuestionBank; if the result is not valid the file is put back.
public class QuestionBankEditor {

    // What the admin typed into the form
    public static class Draft {
        public String type;        // "MC", "ID" or "PS"
        public String topic;
        public int difficulty;     // 1 to 3
        public String text;
        public String answer;      // MC: the letter A to D. ID and PS: the answer text
        public String[] choices;   // MC only, exactly four
        public String[] keywords;  // PS only, at least one
    }

    private static final List<String> TYPES = List.of("MC", "ID", "PS");
    private static final List<String> LETTERS = List.of("A", "B", "C", "D");

    private final Path file;

    public QuestionBankEditor(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public TextFileQuestionBank load() {
        return new TextFileQuestionBank(file);
    }

    // The topics listed in the file's TOPIC lines
    public List<String> topics() {
        List<String> names = new ArrayList<>();
        for (String[] link : load().loadTopicLinks()) {
            if (link.length > 0 && !names.contains(link[0])) {
                names.add(link[0]);
            }
        }
        return names;
    }

    // Returns what is wrong with the draft, or null if it can be saved
    public String check(Draft d) {
        if (d == null || d.type == null || !TYPES.contains(d.type)) {
            return "Pick a question type.";
        }
        String topic = clean(d.topic);
        if (topic.isEmpty() || !topics().contains(topic)) {
            return "Pick a topic from the list.";
        }
        if (d.difficulty < 1 || d.difficulty > 3) {
            return "Pick a difficulty level: 1, 2, or 3.";
        }
        String text = clean(d.text);
        if (text.isEmpty()) {
            return "Type the question.";
        }
        if (text.contains("#")) {
            return "The question cannot contain the # symbol.";
        }

        if (d.type.equals("MC")) {
            if (d.choices == null || d.choices.length != 4) {
                return "Multiple choice needs exactly four choices.";
            }
            for (int i = 0; i < 4; i++) {
                String choice = clean(d.choices[i]);
                char letter = (char) ('A' + i);
                if (choice.isEmpty()) {
                    return "Fill in choice " + letter + ".";
                }
                if (choice.contains("#")) {
                    return "Choice " + letter + " cannot contain the # symbol.";
                }
            }
            if (!LETTERS.contains(clean(d.answer).toUpperCase())) {
                return "Pick which choice is correct.";
            }
            return null;
        }

        String answer = clean(d.answer);
        if (answer.isEmpty()) {
            return d.type.equals("PS") ? "Type the model answer." : "Type the correct answer.";
        }
        if (answer.contains("#")) {
            return "The answer cannot contain the # symbol.";
        }
        if (d.type.equals("PS")) {
            int count = 0;
            if (d.keywords != null) {
                for (String keyword : d.keywords) {
                    String k = clean(keyword);
                    if (k.contains("#")) {
                        return "Keywords cannot contain the # symbol.";
                    }
                    if (!k.isEmpty()) {
                        count++;
                    }
                }
            }
            if (count == 0) {
                return "Add at least one keyword.";
            }
        }
        return null;
    }

    // Adds a new question at the end of its topic's questions and returns its new id
    public int add(Draft d) throws IOException {
        requireValid(d);
        List<String> lines = readLines();

        int highest = 0;
        int insertAt = lines.size();
        String topic = clean(d.topic);
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = questionParts(lines.get(i));
            if (parts == null) {
                continue;
            }
            highest = Math.max(highest, Integer.parseInt(parts[1].trim()));
            if (parts[2].equals(topic)) {
                insertAt = i + 1;
            }
        }
        int id = highest + 1;

        lines.add(insertAt, toLine(id, d));
        save(lines);
        return id;
    }

    // Replaces the question that has this id. The id never changes.
    public void update(int id, Draft d) throws IOException {
        requireValid(d);
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = questionParts(lines.get(i));
            if (parts != null && Integer.parseInt(parts[1].trim()) == id) {
                lines.set(i, toLine(id, d));
                save(lines);
                return;
            }
        }
        throw new IllegalArgumentException("Question " + id + " was not found in the file.");
    }

    // Turns a loaded question back into form values
    public static Draft toDraft(Question q) {
        Draft d = new Draft();
        d.topic = q.getTopic();
        d.difficulty = q.getDifficulty();
        d.text = q.getText();
        d.answer = q.getAnswer();
        if (q instanceof MultipleChoiceQuestion mc) {
            d.type = "MC";
            d.choices = mc.getChoices();
        } else if (q instanceof ProblemSolvingQuestion ps) {
            d.type = "PS";
            d.keywords = ps.getKeywords();
        } else {
            d.type = "ID";
        }
        return d;
    }

    // "MC", "ID" or "PS"
    public static String typeCode(Question q) {
        return toDraft(q).type;
    }

    private void requireValid(Draft d) {
        String problem = check(d);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
    }

    private String toLine(int id, Draft d) {
        List<String> parts = new ArrayList<>();
        parts.add(d.type);
        parts.add(String.valueOf(id));
        parts.add(clean(d.topic));
        parts.add(String.valueOf(d.difficulty));
        parts.add(clean(d.text));
        if (d.type.equals("MC")) {
            for (String choice : d.choices) {
                parts.add(clean(choice));
            }
            parts.add(clean(d.answer).toUpperCase());
        } else {
            parts.add(clean(d.answer));
            if (d.type.equals("PS")) {
                for (String keyword : d.keywords) {
                    if (!clean(keyword).isEmpty()) {
                        parts.add(clean(keyword));
                    }
                }
            }
        }
        return String.join("#", parts);
    }

    // The fields of a question line, or null if the line is not a question
    private static String[] questionParts(String line) {
        String[] parts = line.trim().split("#");
        if (parts.length < 3 || !TYPES.contains(parts[0])) {
            return null;
        }
        try {
            Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        return parts;
    }

    // One question must stay on one line, so line breaks become spaces
    private static String clean(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
    }

    private List<String> readLines() throws IOException {
        return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    private void save(List<String> lines) throws IOException {
        byte[] original = Files.readAllBytes(file);
        writeFile(String.join("\n", lines) + "\n");
        try {
            new TextFileQuestionBank(file);
        } catch (RuntimeException e) {
            Files.write(file, original);
            throw new IllegalStateException("The change was not saved because it made the file invalid ("
                    + e.getMessage() + ").");
        }
    }

    private void writeFile(String content) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, content.getBytes(StandardCharsets.UTF_8));
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
