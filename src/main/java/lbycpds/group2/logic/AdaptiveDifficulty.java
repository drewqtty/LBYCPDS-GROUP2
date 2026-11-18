package lbycpds.group2.logic;

public class AdaptiveDifficulty implements DifficultyPolicy {
    // 1 = easy, 2 = average, 3 = hard
    private static final int MAX_LEVEL = 3;

    // The difficulty goes up only when the score reaches 90%
    @Override
    public int nextLevel(int currentLevel, int score, int totalQuestions) {
        if (totalQuestions <= 0) {
            return currentLevel;
        }

        boolean passed = score * 100 >= StudyFlow.PASS_PERCENT * totalQuestions;
        if (passed && currentLevel < MAX_LEVEL) {
            return currentLevel + 1;
        }

        return currentLevel;
    }
}