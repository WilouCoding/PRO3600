package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AccountManagerTest {
    private AccountManager accountManager;

    @BeforeEach
    public void setUp() {
        // Créer une instance pour les tests
        accountManager = new AccountManager();
    }

    @Test
    public void testCreatePlayerAccount() {
        boolean result = accountManager.createPlayerAccount("testPlayer", "password123");
        assertTrue(result, "Le compte joueur devrait être créé avec succès");
        assertTrue(accountManager.usernameExists("testPlayer"), "Le nom d'utilisateur devrait exister");
    }

    @Test
    public void testCreateDuplicatePlayerAccount() {
        accountManager.createPlayerAccount("duplicate", "pass1");
        boolean result = accountManager.createPlayerAccount("duplicate", "pass2");
        assertFalse(result, "Créer un compte en doublon devrait échouer");
    }

    @Test
    public void testCreateUserAccount() {
        boolean result = accountManager.createUserAccount("testUser", "password123", "test@example.com", "admin");
        assertTrue(result, "Le compte utilisateur devrait être créé avec succès");
        assertTrue(accountManager.usernameExists("testUser"), "Le nom d'utilisateur devrait exister");
    }

    @Test
    public void testAuthenticate() {
        accountManager.createPlayerAccount("auth_test", "correctPass");
        
        assertTrue(accountManager.authenticate("auth_test", "correctPass"), 
                   "L'authentification avec le bon mot de passe devrait réussir");
        assertFalse(accountManager.authenticate("auth_test", "wrongPass"), 
                    "L'authentification avec le mauvais mot de passe devrait échouer");
        assertFalse(accountManager.authenticate("nonexistent", "anyPass"), 
                    "L'authentification pour un compte inexistant devrait échouer");
    }

    @Test
    public void testGetAccount() {
        accountManager.createPlayerAccount("gettest", "pass");
        var account = accountManager.getAccount("gettest");
        
        assertTrue(account.isPresent(), "Le compte devrait être trouvé");
        assertEquals("gettest", account.get().getUsername(), "Le nom d'utilisateur devrait correspondre");
    }

    @Test
    public void testGetNonexistentAccount() {
        var account = accountManager.getAccount("nonexistent");
        assertFalse(account.isPresent(), "Un compte inexistant ne devrait pas être trouvé");
    }

    @Test
    public void testUsernameExists() {
        assertFalse(accountManager.usernameExists("unknown"));
        accountManager.createPlayerAccount("known", "pass");
        assertTrue(accountManager.usernameExists("known"));
    }

    @Test
    public void testUpdatePlayerScore() {
        accountManager.createPlayerAccount("scorePlayer", "pass");
        accountManager.updatePlayerScore("scorePlayer", 1500);
        
        var account = accountManager.getAccount("scorePlayer");
        assertTrue(account.isPresent());
        assertTrue(account.get() instanceof PlayerAccount);
        PlayerAccount playerAccount = (PlayerAccount) account.get();
        assertEquals(1500, playerAccount.getBestScore(), "Le meilleur score devrait être mis à jour");
    }

    @Test
    public void testUpdateNonPlayerAccountScore() {
        // Vérifier que ça ne crash pas si on essaie de mettre à jour un compte utilisateur
        accountManager.createUserAccount("normalUser", "pass", "email@test.com", "user");
        accountManager.updatePlayerScore("normalUser", 1000); // Ne devrait rien faire
        
        var account = accountManager.getAccount("normalUser");
        assertTrue(account.isPresent());
        assertTrue(account.get() instanceof UserAccount);
    }
}
