package doodlejump;

import java.io.*;
import java.util.*;

public class HighScoreManager {
    private static final String FILE_NAME = "highscores.txt";
    private List<Integer> scores;

    public HighScoreManager() {
        scores = new ArrayList<>();
        loadScores();
    }

    
    public void addScore(int score) {
        scores.add(score);
        scores.sort(Collections.reverseOrder()); // Trie du plus grand au plus petit
        
        if (scores.size() > 5) {
            scores = scores.subList(0, 5); // Coupe la liste pour ne garder que les 5 premiers
        }
        saveScores(); // Sauvegarde dans le fichier
    }

    // Récupère le meilleur score absolu
    public int getBestScore() {
        if (scores.isEmpty()) return 0;
        return scores.get(0);
    }

    // Récupère la liste complète pour l'écran de fin
    public List<Integer> getTop5() {
        return scores;
    }

    private void loadScores() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return; // Si le jeu est lancé pour la 1ère fois, pas de fichier
            
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextInt()) {
                scores.add(scanner.nextInt());
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement des scores.");
        }
    }

    private void saveScores() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME));
            for (int s : scores) {
                writer.println(s);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Erreur lors de la sauvegarde des scores.");
        }
    }
}