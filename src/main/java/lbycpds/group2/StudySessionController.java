package lbycpds.group2;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.logic.*;
import lbycpds.group2.model.Session;
import lbycpds.group2.model.Student;
import lbycpds.group2.structures.CircularQueue;
import lbycpds.group2.structures.Graph;

import java.util.List;
import java.util.Map;

public class StudySessionController {
    private final TopicStore topicStore;
    private final SessionStore sessionStore;
    private final QuestionHistoryStore historyStore;
    private final DifficultyPolicy difficultyPolicy;

    public StudySessionController(TopicStore topicStore,
                                  SessionStore sessionStore,
                                  QuestionHistoryStore historyStore,
                                  DifficultyPolicy difficultyPolicy) {
        this.topicStore = topicStore;
        this.sessionStore = sessionStore;
        this.historyStore = historyStore;
        this.difficultyPolicy = difficultyPolicy;
    }

    /**
     * Creates a new session based on the specified mode (NORMAL, CRAM, MISSED).
     */
    public StudySession createSession(StudyMode mode,
                                      Student student,
                                      String targetCourse,
                                      Graph progressionGraph,
                                      CircularQueue<String> courseQueue,
                                      Map<String, Graph> courseGraphs) {

        switch (mode) {
            case NORMAL:
                if (targetCourse == null || targetCourse.trim().isEmpty()) {
                    throw new IllegalArgumentException("Normal mode requires a valid target course.");
                }
                return new SingleCourseStudySession(
                        student, topicStore, sessionStore, historyStore,
                        targetCourse, difficultyPolicy, progressionGraph
                );

            case CRAM:
                return createCramSession(student, courseGraphs);

            case MISSED:
                if (targetCourse == null || targetCourse.trim().isEmpty()) {
                    throw new IllegalArgumentException("Missed questions review requires a target course.");
                }
                return new MissedQuestionsStudySession(
                        student, topicStore, sessionStore, historyStore,
                        targetCourse
                );

            default:
                throw new IllegalArgumentException("Unsupported study mode: " + mode);
        }
    }

    public CramStudySession createCramSession(Student student, Map<String, Graph> courseGraphs) {
        return new CramStudySession(
                student, topicStore, sessionStore, historyStore,
                difficultyPolicy, courseGraphs
        );
    }

    public StudySession continueLastSession(Student student, Map<String, Graph> courseGraphs) {
        List<String> studentCourses = student.getCourses();
        String courseToResume = (studentCourses != null && !studentCourses.isEmpty()) ? studentCourses.get(0) : "DEFAULT";

        if (sessionStore != null && studentCourses != null && !studentCourses.isEmpty()) {
            Session latestSession = null;

            for (String course : studentCourses) {
                List<Session> courseSessions = sessionStore.loadSessions(student.getName(), course);
                if (courseSessions != null && !courseSessions.isEmpty()) {
                    Session candidate = courseSessions.get(courseSessions.size() - 1);

                    if (latestSession == null || candidate.getDate().isAfter(latestSession.getDate())) {
                        latestSession = candidate;
                        courseToResume = course;
                    }
                }
            }
        }

        Graph graph = courseGraphs != null ? courseGraphs.get(courseToResume) : null;

        return new SingleCourseStudySession(
                student, topicStore, sessionStore, historyStore,
                courseToResume, difficultyPolicy, graph
        );
    }
}
