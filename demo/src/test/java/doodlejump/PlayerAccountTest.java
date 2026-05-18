package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerAccountTest {
    private PlayerAccount account;

    @BeforeEach
    public void setUp() {
        account = new PlayerAccount("testPlayer", "password123");
    }

    @Test
    public void testInitialBestScore() {
        assertEquals(0, account.getBestScore(), "Le meilleur score initial devrait être 0");
    }

    @Test
    public void testSetBestScore() {
        account.setBestScore(500);
        assertEquals(500, account.getBestScore(), "Le meilleur score devrait être 500");
    }

    @Test
    public void testSetBestScoreHigher() {
        account.setBestScore(300);
        account.setBestScore(500);
        assertEquals(500, account.getBestScore(), "Le meilleur score devrait être mis à jour vers le plus élevé");
    }

    @Test
    public void testSetBestScoreLower() {
        account.setBestScore(500);
        account.setBestScore(300);
        assertEquals(500, account.getBestScore(), "Le meilleur score ne devrait pas diminuer");
    }

    @Test
    public void testGetUsername() {
        assertEquals("testPlayer", account.getUsername(), "Le nom d'utilisateur devrait être 'testPlayer'");
    }

    @Test
    public void testGetType() {
        assertEquals(AccountType.PLAYER, account.getType(), "Le type devrait être PLAYER");
    }

    @Test
    public void testPasswordVerification() {
        assertTrue(account.verifyPassword("password123"), "La vérification du bon mot de passe devrait réussir");
        assertFalse(account.verifyPassword("wrongPassword"), "La vérification du mauvais mot de passe devrait échouer");
    }

    @Test
    public void testSerialize() {
        account.setBestScore(1000);
        String serialized = account.serialize();
        
        assertTrue(serialized.contains("PLAYER"), "Le type devrait être dans la sérialisation");
        assertTrue(serialized.contains("testPlayer"), "Le nom d'utilisateur devrait être dans la sérialisation");
        assertTrue(serialized.contains("1000"), "Le score devrait être dans la sérialisation");
    }

    @Test
    public void testMultipleScoreUpdates() {
        account.setBestScore(100);
        account.setBestScore(200);
        account.setBestScore(150);
        account.setBestScore(300);
        account.setBestScore(250);
        
        assertEquals(300, account.getBestScore(), "Le meilleur score devrait être le maximum de tous les scores");
    }

    @Test
    public void testSetBestScoreWithZero() {
        account.setBestScore(100);
        account.setBestScore(0);
        assertEquals(100, account.getBestScore(), "Zéro ne devrait pas remplacer un meilleur score");
    }

    @Test
    public void testSetBestScoreWithNegative() {
        account.setBestScore(100);
        account.setBestScore(-50);
        assertEquals(100, account.getBestScore(), "Les scores négatifs ne devraient pas remplacer un meilleur score");
    }

    @Test
    public void testDifferentUsernamesAreIndependent() {
        PlayerAccount account2 = new PlayerAccount("player2", "pass456");
        
        account.setBestScore(500);
        account2.setBestScore(300);
        
        assertEquals(500, account.getBestScore());
        assertEquals(300, account2.getBestScore());
    }
}
