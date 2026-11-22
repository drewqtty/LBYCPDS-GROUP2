package lbycpds.group2.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// The one place that lists the courses Cram offers.
// Every study mode (Regular, Cram, Tutor, Missed review) and the Admin editor get their courses from here.
//
// BACKEND INTEGRATION POINT:
//   To add a course, add its question bank path to BANK_RESOURCES and add a lesson file with the
//   same file name in resources/lessons/. To change where course content comes from, change how
//   open() builds a TextFileQuestionBank (it only needs to be a QuestionSource).
//   No screen needs to change.
public class CourseCatalog {

    private static final String[] BANK_RESOURCES = {
            "/questions/circuits.txt",
            "/questions/dsa.txt",
            "/questions/chemistry.txt"
    };

    // TEMPORARY MOCK DATA: a course whose real bank has no questions yet falls back to a small
    // sample bank so the screens can be demonstrated. As soon as the real bank has questions the
    // sample is no longer used. Delete this map and the *-mock files when real content is in.
    private static final Map<String, String> SAMPLE_BANKS = Map.of(
            "/questions/chemistry.txt", "/questions/chemistry-mock.txt");

    // One course: the bank its questions come from and whether that bank is the temporary sample
    public static class Course {
        private final String resourcePath;
        private final TextFileQuestionBank bank;

        Course(String resourcePath, TextFileQuestionBank bank) {
            this.resourcePath = resourcePath;
            this.bank = bank;
        }

        public String getName() {
            return bank.getCourse();
        }

        // Where the questions were read from, e.g. "/questions/dsa.txt".
        // LessonRepository.forQuestionBank(...) turns it into the matching lesson file.
        public String getResourcePath() {
            return resourcePath;
        }

        public TextFileQuestionBank getBank() {
            return bank;
        }

        // True while the course is showing temporary sample content instead of real content
        public boolean isSample() {
            return sample;
        }

        public boolean hasQuestions() {
            return bank.loadQuestions().length > 0;
        }
    }

    // Every course, in the order they appear in the course list
    public static List<Course> all() {
        List<Course> courses = new ArrayList<>();
        for (String resource : BANK_RESOURCES) {
            courses.add(open(resource));
        }
        return courses;
    }

    // The course with this name, or null if there is none
    public static Course find(String name) {
        if (name == null) {
            return null;
        }
        for (String resource : BANK_RESOURCES) {
            Course course = open(resource);
            if (course.getName().equals(name)) {
                return course;
            }
        }
        return null;
    }

    // The real question bank files, for the Admin editor (it never edits the temporary samples)
    public static String[] bankResources() {
        return BANK_RESOURCES.clone();
    }

    private static Course open(String resource) {
        return new Course(resource, QuestionBankFiles.open(resource));
    }
}
