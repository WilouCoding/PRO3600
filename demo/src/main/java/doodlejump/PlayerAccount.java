package doodlejump;

public class PlayerAccount extends Account {
    private int bestScore;

    public PlayerAccount(String username, String password) {
        super(username, password, AccountType.PLAYER);
        this.bestScore = 0;
    }

    public PlayerAccount(String username, String passwordHash, int bestScore, boolean isHash) {
        super(username, passwordHash, AccountType.PLAYER, true);
        this.bestScore = bestScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        if (bestScore > this.bestScore) {
            this.bestScore = bestScore;
        }
    }

    @Override
    public String serialize() {
        return String.format("%s|%s|%s|%d", type, username, passwordHash, bestScore);
    }
}
