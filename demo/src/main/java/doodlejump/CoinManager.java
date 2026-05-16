package doodlejump;

import java.io.*;
import java.util.*;

public class CoinManager {
    private static final String FILE_NAME = "coins.txt";
    private int totalCoins = 0;

    public CoinManager() {
        loadCoins();
    }

    public void addCoins(int amount) {
        totalCoins += amount;
        saveCoins();
    }

    public int getCoins() {
        return totalCoins;
    }

    private void loadCoins() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return;
            Scanner scanner = new Scanner(file);
            if (scanner.hasNextInt()) totalCoins = scanner.nextInt();
            scanner.close();
        } catch (Exception e) {
            System.out.println("Erreur chargement pièces.");
        }
    }

    private void saveCoins() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME));
            writer.println(totalCoins);
            writer.close();
        } catch (Exception e) {
            System.out.println("Erreur sauvegarde pièces.");
        }
    }
}