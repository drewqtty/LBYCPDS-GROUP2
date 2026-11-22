package lbycpds.group2.logic;

import lbycpds.group2.model.Lesson;
import lbycpds.group2.model.LessonPage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Loads the lessons of one course from a text file in src/main/resources/lessons/.
// To add a lesson or a whole course, edit or add a file there. No Java code needs to change.
//
// File format (see docs/lesson-format.md):
//   COURSE#course name
//   CATEGORY#group name shown on the topic screen
//   LESSON#lesson title#question bank topic for the practice quiz (may be left out)
//   PAGE#page title
//   ...page text, as many lines as needed...
//   PAGE#next page title
//
// Inside a ``` code block, lines are never read as COURSE, CATEGORY, LESSON, PAGE or comments.
// Outside code blocks, lines starting with // are skipped.
public class LessonRepository {

    private String course = "";
    private final List<Lesson> lessons = new ArrayList<>();

    // A repository with no lessons
    public LessonRepository() {
    }

    // Reads the file at resourcePath. A missing file gives an empty repository (a course with no lessons yet).
    public LessonRepository(String resourcePath) {
        InputStream stream = LessonRepository.class.getResourceAsStream(resourcePath);
        if (stream != null) {
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                read(reader);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read " + resourcePath);
            }
        }
    }

    // The lessons that go with a question bank: "/questions/dsa.txt" uses "/lessons/dsa.txt".
    // A course without a lesson file gets an empty repository.
    public static LessonRepository forQuestionBank(String questionResourcePath) {
        String name = questionResourcePath.substring(questionResourcePath.lastIndexOf('/') + 1);
        return new LessonRepository("/lessons/" + name);
    }

    // Reads lessons from text already in memory (used by the tests)
    public static LessonRepository fromText(String text) {
        LessonRepository repository = new LessonRepository();
        try {
            repository.read(new StringReader(text));
        } catch (IOException e) {
            throw new IllegalStateException("Could not read lesson text");
        }
        return repository;
    }

    public String getCourse() {
        return course;
    }

    public boolean isEmpty() {
        return lessons.isEmpty();
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    // Category names in the order they first appear in the file
    public List<String> getCategories() {
        List<String> names = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (!names.contains(lesson.getCategory())) {
                names.add(lesson.getCategory());
            }
        }
        return names;
    }

    public List<Lesson> getLessonsIn(String category) {
        List<Lesson> found = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (lesson.getCategory().equals(category)) {
                found.add(lesson);
            }
        }
        return found;
    }

    public Lesson findByTitle(String title) {
        for (Lesson lesson : lessons) {
            if (lesson.getTitle().equals(title)) {
                return lesson;
            }
        }
        return null;
    }

    private void read(Reader source) throws IOException {
        BufferedReader reader = new BufferedReader(source);
        String category = "General";
        Lesson lesson = null;
        String pageTitle = null;
        StringBuilder body = new StringBuilder();
        boolean inCode = false;
        int lineNumber = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (line.trim().startsWith("```")) {
                inCode = !inCode;
            }

            if (!inCode && !line.trim().startsWith("```")) {
                if (line.startsWith("//")) {
                    continue;
                }
                if (line.startsWith("COURSE#")) {
                    course = line.substring("COURSE#".length()).trim();
                    continue;
                }
                if (line.startsWith("CATEGORY#")) {
                    savePage(lesson, pageTitle, body);
                    pageTitle = null;
                    category = line.substring("CATEGORY#".length()).trim();
                    if (category.isEmpty()) {
                        throw new IllegalArgumentException("Bad line " + lineNumber + " (category name is empty)");
                    }
                    continue;
                }
                if (line.startsWith("LESSON#")) {
                    savePage(lesson, pageTitle, body);
                    pageTitle = null;
                    String[] parts = line.split("#", -1);
                    String title = parts[1].trim();
                    if (title.isEmpty()) {
                        throw new IllegalArgumentException("Bad line " + lineNumber + " (lesson title is empty)");
                    }
                    if (findByTitle(title) != null) {
                        throw new IllegalArgumentException("Bad line " + lineNumber + " (duplicate lesson " + title + ")");
                    }
                    String practiceTopic = parts.length > 2 ? parts[2].trim() : "";
                    lesson = new Lesson(title, category, practiceTopic);
                    lessons.add(lesson);
                    continue;
                }
                if (line.startsWith("PAGE#")) {
                    if (lesson == null) {
                        throw new IllegalArgumentException("Bad line " + lineNumber + " (PAGE before any LESSON)");
                    }
                    savePage(lesson, pageTitle, body);
                    pageTitle = line.substring("PAGE#".length()).trim();
                    if (pageTitle.isEmpty()) {
                        throw new IllegalArgumentException("Bad line " + lineNumber + " (page title is empty)");
                    }
                    continue;
                }
            }

            // Text before the first PAGE of a lesson is ignored
            if (pageTitle != null) {
                body.append(line).append('\n');
            }
        }

        if (inCode) {
            throw new IllegalArgumentException("A ``` code block was never closed");
        }
        savePage(lesson, pageTitle, body);
    }

    // Adds the page being read to its lesson and clears the text buffer
    private void savePage(Lesson lesson, String pageTitle, StringBuilder body) {
        if (lesson != null && pageTitle != null) {
            String text = body.toString().strip();
            if (!text.isEmpty()) {
                lesson.addPage(new LessonPage(pageTitle, text));
            }
        }
        body.setLength(0);
    }
}