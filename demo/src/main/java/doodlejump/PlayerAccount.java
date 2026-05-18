package doodlejump;

public class PlayerAccount extends Account {
    private int bestScore;
    private String equippedSkinId;

    public PlayerAccount(String username, String password) {
        super(username, password, AccountType.PLAYER);
        this.bestScore = 0;
        this.equippedSkinId = "skin_default";
    }

    public PlayerAccount(String username, String passwordHash, int bestScore, String equippedSkinId, boolean isHash) {
        super(username, passwordHash, AccountType.PLAYER, true);
        this.bestScore = bestScore;
        this.equippedSkinId = equippedSkinId == null || equippedSkinId.isBlank() ? "skin_default" : equippedSkinId;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        if (bestScore > this.bestScore) {
            this.bestScore = bestScore;
        }
    }

    public String getEquippedSkinId() {
        return equippedSkinId;
    }

    public void setEquippedSkinId(String equippedSkinId) {
        if (equippedSkinId != null && !equippedSkinId.isBlank()) {
            this.equippedSkinId = equippedSkinId;
        }
    }

    @Override
    public String serialize() {
        return String.format("%s|%s|%s|%d|%s", type, username, passwordHash, bestScore, equippedSkinId);
    }
}
