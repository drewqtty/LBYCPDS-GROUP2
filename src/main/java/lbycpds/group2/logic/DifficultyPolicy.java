package lbycpds.group2.logic;

public interface DifficultyPolicy {
    int nextLevel(int currentLevel, int score, int totalQuestions);
}