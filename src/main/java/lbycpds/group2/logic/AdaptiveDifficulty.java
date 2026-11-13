package lbycpds.group2.logic;

public class AdaptiveDifficulty implements DifficultyPolicy{
    // 1 = easy, 2 = average, 3 = hard
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 3;

    private static final double UPGRADE_THRESHOLD = 0.85; // 85% or higher
    private static final double DOWNGRADE_THRESHOLD = 0.50; // Below 50%

    @Override
    public int nextLevel(int currentLevel, int score, int totalQuestions) {
        if (totalQuestions <= 0) {
            return currentLevel;
        }

        double scorePercentage = (double) score / totalQuestions;

        // increasing the difficulty if the user is performing very well
        if (scorePercentage >= UPGRADE_THRESHOLD && currentLevel < MAX_LEVEL) {
            return currentLevel + 1;
        }

        // lowering the difficulty if the user struggles
        else if (scorePercentage < DOWNGRADE_THRESHOLD && currentLevel > MIN_LEVEL) {
            return currentLevel - 1;
        }

        return currentLevel;
    }
}
