package lbycpds.group2.logic;

import lbycpds.group2.model.IdentificationQuestion;
import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.ProblemSolvingQuestion;
import lbycpds.group2.model.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextFileQuestionBank implements QuestionSource {
    private String course;
    private List<Question> questions;
    private List<String[]> topicLinks;

    public TextFileQuestionBank(String resourcePath) {
        course = "";
        questions = new ArrayList<>();
        topicLinks = new ArrayList<>();
        readFile(resourcePath);
    }

    public String getCourse() {
        return course;
    }

    public Question[] loadQuestions() {
        return questions.toArray(new Question[0]);
    }

    public String[][] loadTopicLinks() {
        return topicLinks.toArray(new String[0][]);
    }

    private void readFile(String resourcePath) {
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Question file not found: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                parseLine(line, lineNumber);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resourcePath);
        }
    }

    private void parseLine(String line, int lineNumber) {
        String[] parts = line.split("#");
        String code = parts[0];
        try {
            if (!code.equals("COURSE") && course.isEmpty()) {
                throw new IllegalArgumentException("COURSE line must come first");
            }
            if (code.equals("COURSE")) {
                course = parts[1];
            } else if (code.equals("TOPIC")) {
                topicLinks.add(copyFrom(parts, 1));
            } else if (code.equals("MC")) {
                addQuestion(buildMultipleChoice(parts));
            } else if (code.equals("ID")) {
                addQuestion(buildIdentification(parts));
            } else if (code.equals("PS")) {
                addQuestion(buildProblemSolving(parts));
            } else {
                throw new IllegalArgumentException("Unknown line type");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Bad line " + lineNumber + " (missing field): " + line);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Bad line " + lineNumber + " (" + e.getMessage() + "): " + line);
        }
    }

    private Question buildMultipleChoice(String[] parts) {
        int id = Integer.parseInt(parts[1]);
        String topic = parts[2];
        int difficulty = Integer.parseInt(parts[3]);
        String text = parts[4];
        String[] choices = {parts[5], parts[6], parts[7], parts[8]};
        String answer = parts[9];
        if (parts.length != 10) {
            throw new IllegalArgumentException("Multiple choice needs 10 fields but has " + parts.length);
        }
        return new MultipleChoiceQuestion(id, course, topic, difficulty, text, answer, choices);
    }

    private Question buildIdentification(String[] parts) {
        int id = Integer.parseInt(parts[1]);
        String topic = parts[2];
        int difficulty = Integer.parseInt(parts[3]);
        String text = parts[4];
        String answer = parts[5];
        if (parts.length != 6) {
            throw new IllegalArgumentException("Identification needs 6 fields but has " + parts.length);
        }
        return new IdentificationQuestion(id, course, topic, difficulty, text, answer);
    }

    private Question buildProblemSolving(String[] parts) {
        int id = Integer.parseInt(parts[1]);
        String topic = parts[2];
        int difficulty = Integer.parseInt(parts[3]);
        String text = parts[4];
        String answer = parts[5];
        String[] keywords = copyFrom(parts, 6);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Problem solving needs at least 7 fields but has " + parts.length);
        }
        return new ProblemSolvingQuestion(id, course, topic, difficulty, text, answer, keywords);
    }

    private void addQuestion(Question question) {
        if (question.getDifficulty() < 1 || question.getDifficulty() > 3) {
            throw new IllegalArgumentException("Difficulty must be 1 to 3");
        }
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getId() == question.getId()) {
                throw new IllegalArgumentException("Duplicate id " + question.getId());
            }
        }
        questions.add(question);
    }

    private String[] copyFrom(String[] parts, int start) {
        String[] result = new String[parts.length - start];
        for (int i = start; i < parts.length; i++) {
            result[i - start] = parts[i];
        }

        return result;
    }
}