package doodlejump;

import java.io.*;
import java.util.*;

public class AccountManager {
    private static final String FILE_NAME = "accounts.txt";
    private final Map<String, Account> accounts = new HashMap<>();

    public AccountManager() {
        loadAccounts();
    }

    public Optional<Account> getAccount(String username) {
        return Optional.ofNullable(accounts.get(username));
    }

    public boolean usernameExists(String username) {
        return accounts.containsKey(username);
    }

    public boolean createPlayerAccount(String username, String password) {
        if (usernameExists(username)) {
            return false;
        }
        PlayerAccount account = new PlayerAccount(username, password);
        accounts.put(username, account);
        saveAccounts();
        return true;
    }

    public boolean createUserAccount(String username, String password, String email, String role) {
        if (usernameExists(username)) {
            return false;
        }
        UserAccount account = new UserAccount(username, password, email, role);
        accounts.put(username, account);
        saveAccounts();
        return true;
    }

    public boolean authenticate(String username, String password) {
        Account account = accounts.get(username);
        return account != null && account.verifyPassword(password);
    }

    public void updatePlayerScore(String username, int score) {
        Account account = accounts.get(username);
        if (account instanceof PlayerAccount playerAccount) {
            playerAccount.setBestScore(score);
            saveAccounts();
        }
    }

    public String getPlayerEquippedSkinId(String username) {
        Account account = accounts.get(username);
        if (account instanceof PlayerAccount playerAccount) {
            return playerAccount.getEquippedSkinId();
        }
        return null;
    }

    public void setPlayerEquippedSkin(String username, String skinId) {
        Account account = accounts.get(username);
        if (account instanceof PlayerAccount playerAccount) {
            playerAccount.setEquippedSkinId(skinId);
            saveAccounts();
        }
    }

    private void loadAccounts() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Account account = Account.deserialize(line);
                if (account != null) {
                    accounts.put(account.getUsername(), account);
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible de charger les comptes : " + e.getMessage());
        }
    }

    private void saveAccounts() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Account account : accounts.values()) {
                writer.println(account.serialize());
            }
        } catch (IOException e) {
            System.out.println("Impossible de sauvegarder les comptes : " + e.getMessage());
        }
    }
}
