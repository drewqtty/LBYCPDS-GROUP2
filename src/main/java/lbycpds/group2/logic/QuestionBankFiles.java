package lbycpds.group2.logic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Finds the editable copy of each question bank.
// The files inside src/main/resources are read-only once the app is packaged, so the first
// time a bank is opened it is copied to a folder the admin can write to:
//   ~/.cram/questions/   (or the folder given with -Dcram.questions.dir=...)
public class QuestionBankFiles {

    public static Path directory() {
        String override = System.getProperty("cram.questions.dir");
        if (override != null && !override.isBlank()) {
            return Paths.get(override);
        }
        return Paths.get(System.getProperty("user.home"), ".cram", "questions");
    }

    // resourcePath looks like "/questions/circuits.txt". Creates the copy if it does not exist yet.
    public static Path fileFor(String resourcePath) throws IOException {
        String name = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        Path file = directory().resolve(name);
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            try (InputStream in = QuestionBankFiles.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IOException("Bundled question file not found: " + resourcePath);
                }
                Files.copy(in, file);
            }
        }
        return file;
    }

    // Opens the editable copy, or the bundled file if the folder cannot be used
    public static TextFileQuestionBank open(String resourcePath) {
        Path file;
        try {
            file = fileFor(resourcePath);
        } catch (IOException e) {
            return new TextFileQuestionBank(resourcePath);
        }
        return new TextFileQuestionBank(file);
    }
}
