package lbycpds.group2;

import lbycpds.group2.database.CramStore;
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
    private CramStore cramStore;

    public StudySessionController(TopicStore topicStore,
                                  SessionStore sessionStore,
                                  QuestionHistoryStore historyStore,
                                  DifficultyPolicy difficultyPolicy) {
        this.topicStore = topicStore;
        this.sessionStore = sessionStore;
        this.historyStore = historyStore;
        this.difficultyPolicy = difficultyPolicy;
    }

    public void setCramStore(CramStore cramStore) {
        this.cramStore = cramStore;
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
                if (targetCourse == null || targetCourse.trim().isEmpty()) {
                    throw new IllegalArgumentException("Cram mode requires a valid target course.");
                }
                return createCramSession(student, targetCourse);

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

    public CramStudySession createCramSession(Student student, String course) {
        return new CramStudySession(
                student, topicStore, sessionStore, historyStore, cramStore, course
        );
    }

    /**
     * Continues where the student stopped: the course they studied last, starting on the
     * topic they were on. Falls back to the course with the latest saved session.
     * Returns null when the student has nothing saved to continue.
     */
    public SingleCourseStudySession continueLastSession(Student student, Map<String, Graph> courseGraphs) {
        List<String> studentCourses = student.getCourses();
        String courseToResume = null;

        if (topicStore != null) {
            courseToResume = topicStore.loadLastCourse(student.getName());
        }

        if (courseToResume == null && sessionStore != null && studentCourses != null) {
            Session latestSession = null;
            for (String course : studentCourses) {
                List<Session> courseSessions = sessionStore.loadSessions(student.getName(), course);
                if (courseSessions != null && !courseSessions.isEmpty()) {
                    Session candidate = courseSessions.get(courseSessions.size() - 1);
                    if (latestSession == null || !candidate.getDate().isBefore(latestSession.getDate())) {
                        latestSession = candidate;
                        courseToResume = course;
                    }
                }
            }
        }

        if (courseToResume == null) {
            return null;
        }

        Graph graph = courseGraphs != null ? courseGraphs.get(courseToResume) : null;
        SingleCourseStudySession session = new SingleCourseStudySession(
                student, topicStore, sessionStore, historyStore,
                courseToResume, difficultyPolicy, graph
        );
        if (topicStore != null) {
            session.setStartTopic(topicStore.loadLastTopic(student.getName(), courseToResume));
        }
        return session;
    }
}