package doodlejump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountValidationTest {

    @Test
    public void testHashPasswordProducesStableHash() {
        String hash1 = Account.hashPassword("password123");
        String hash2 = Account.hashPassword("password123");

        assertNotNull(hash1, "Le hash ne doit pas être nul");
        assertEquals(hash1, hash2, "Le hash doit être stable pour la même entrée");
        assertNotEquals("password123", hash1, "Le hash ne doit pas être identique au mot de passe en clair");
    }

    @Test
    public void testVerifyPasswordWithCorrectAndIncorrectPassword() {
        PlayerAccount account = new PlayerAccount("player", "secret");

        assertTrue(account.verifyPassword("secret"), "La vérification du bon mot de passe doit réussir");
        assertFalse(account.verifyPassword("wrong"), "La vérification d'un mauvais mot de passe doit échouer");
    }

    @Test
    public void testDeserializeNullOrBlankReturnsNull() {
        assertNull(Account.deserialize(null), "La désérialisation d'une chaîne nulle doit retourner null");
        assertNull(Account.deserialize(""), "La désérialisation d'une chaîne vide doit retourner null");
        assertNull(Account.deserialize("   "), "La désérialisation d'une chaîne blanche doit retourner null");
    }

    @Test
    public void testDeserializeInvalidFormatReturnsNull() {
        assertNull(Account.deserialize("INVALID|FORMAT"), "La désérialisation d'un format invalide doit retourner null");
        assertNull(Account.deserialize("UNKNOWN|user|hash"), "La désérialisation d'un type inconnu doit retourner null");
    }

    @Test
    public void testPlayerAccountDeserializeRoundTrip() {
        PlayerAccount original = new PlayerAccount("player1", "pass123");
        original.setBestScore(800);
        String serialized = original.serialize();

        Account deserialized = Account.deserialize(serialized);
        assertNotNull(deserialized, "La désérialisation d'un compte joueur valide ne doit pas retourner null");
        assertTrue(deserialized instanceof PlayerAccount, "L'objet désérialisé doit être un PlayerAccount");

        PlayerAccount playerAccount = (PlayerAccount) deserialized;
        assertEquals("player1", playerAccount.getUsername());
        assertEquals(AccountType.PLAYER, playerAccount.getType());
        assertTrue(playerAccount.verifyPassword("pass123"), "Le mot de passe désérialisé doit être vérifié correctement");
        assertEquals(800, playerAccount.getBestScore());
    }

    @Test
    public void testUserAccountDeserializeRoundTrip() {
        UserAccount original = new UserAccount("user1", "secret", "user@example.com", "admin");
        String serialized = original.serialize();

        Account deserialized = Account.deserialize(serialized);
        assertNotNull(deserialized, "La désérialisation d'un compte utilisateur valide ne doit pas retourner null");
        assertTrue(deserialized instanceof UserAccount, "L'objet désérialisé doit être un UserAccount");

        UserAccount userAccount = (UserAccount) deserialized;
        assertEquals("user1", userAccount.getUsername());
        assertEquals(AccountType.USER, userAccount.getType());
        assertTrue(userAccount.verifyPassword("secret"), "Le mot de passe désérialisé doit être vérifié correctement");
        assertEquals("user@example.com", userAccount.getEmail());
        assertEquals("admin", userAccount.getRole());
    }
}
