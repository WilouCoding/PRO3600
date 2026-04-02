package doodlejump;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.List;

public class GamePanel extends Canvas {
    
    private int score = 0;
    private int maxClimb = 0;
    private int startY;
    private boolean isGameOver = false;
    private HighScoreManager highScoreManager; 

    public GamePanel(int startY) {
        super(400, 600);
        this.startY = startY;
        this.highScoreManager = new HighScoreManager(); // Charge les scores au démarrage
        drawScreen(); 
    }

    public void updateScore(double currentGoonerY) {
        if (isGameOver) return;

        int currentClimb = startY - (int) currentGoonerY;
        if (currentClimb > maxClimb) {
            maxClimb = currentClimb;
            score = maxClimb / 10;
            drawScreen();
        }
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
        
        if (gameOver) {
            highScoreManager.addScore(score); 
        }
        drawScreen(); 
    }

    public void drawScreen() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (isGameOver) {
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", 40));
            gc.fillText("GAME OVER", 80, 150);
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 25));
            gc.fillText("Score Final : " + score, 120, 200);
            
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font("Arial", 20));
            gc.fillText("--- TOP 5 ---", 140, 250);
            
            gc.setFill(Color.WHITE);
            List<Integer> topScores = highScoreManager.getTop5();
            for (int i = 0; i < topScores.size(); i++) {
                gc.fillText((i + 1) + ". " + topScores.get(i), 160, 285 + (i * 25));
            }
            

            gc.setFont(Font.font("Arial", 15));
            gc.fillText("Appuyez sur Espace pour recommencer", 60, 450);
        } else {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            
            int displayBest = Math.max(score, highScoreManager.getBestScore());
            gc.fillText("Meilleur Score: " + displayBest, 10, 30);
            gc.fillText("Score: " + score, 10, 55);
        }
    }

    public void reset() {
        maxClimb = 0;
        score = 0;
        isGameOver = false; 
        drawScreen();       
    }
}