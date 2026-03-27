package doodlejump;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GamePanel extends Canvas {
    
    private int score = 0;
    private int maxClimb = 0;
    private int startY;
    private boolean isGameOver = false; // On ajoute l'état

    public GamePanel(int startY) {
        super(400, 600); // Important : on prend toute la taille de l'écran
        this.startY = startY;
        drawScreen(); 
    }

    public void updateScore(double currentGoonerY) {
        if (isGameOver) return; // On bloque le score si on est mort

        int currentClimb = startY - (int) currentGoonerY;
        if (currentClimb > maxClimb) {
            maxClimb = currentClimb;
            score = maxClimb / 10;
            drawScreen();
        }
    }

    // NOUVELLE MÉTHODE : Déclencher le Game Over
    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        drawScreen(); // On redessine l'écran pour afficher le texte
    }

    // On renomme drawScore en drawScreen car elle gère plus de choses
    public void drawScreen() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight()); // Efface l'ancien texte

        if (isGameOver) {
            // Affichage de l'écran de fin (copié de ton GameView)
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", 40));
            gc.fillText("GAME OVER", 80, 250);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 25));
            gc.fillText("Score Final : " + score, 120, 300);
            
            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Appuyez sur Espace pour recommencer", 60, 350);
        } else {
            // Affichage du score normal en jeu
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            gc.fillText("Score: " + score, 10, 30);
        }
    }

    public void reset() {
    maxClimb = 0;
    score = 0;
    isGameOver = false; 
    drawScreen();       
    }
}