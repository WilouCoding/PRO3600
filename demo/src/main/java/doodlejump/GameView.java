    package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;

import java.util.*;



public class GameView extends Pane {
    private boolean isGameOver = false;
    private Canvas canvas = new Canvas(400, 600);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private double standY = 280;
    private double standX = 180;
    private List<Platform> platforms = new ArrayList<>();
    Gooner goon = new Gooner(standX, standY);
    
    private double cameraY = 0;
    private Random rand = new Random();
    private GamePanel scorePanel;
    
    public GameView() {
        getChildren().add(canvas);
        scorePanel = new GamePanel((int) standY);
        getChildren().add(scorePanel); // Ajoute la fenêtre de score par-dessus le jeu
        generatePlatform(platforms);
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now){
                
                goon.update();
                scorePanel.updateScore(goon.y);

                if (goon.velocityY > 0){
                    for (Platform p : platforms){ // collision
                        if (goon.x < p.x + p.WIDTH 
                            && goon.x + Gooner.w > p.x 
                            && goon.y + Gooner.h >= p.y
                            && goon.y + Gooner.h <= p.y + p.HEIGHT){
                                goon.jump();
                                //ajouter du code afin que le décor descendre et que les plateformes soient régénérées
                        }
                    }

                }
                if (goon.y >cameraY + 600) {
                    isGameOver =true;
                    scorePanel.setGameOver(true);
                }
                if (goon.y < cameraY + 350){
                    cameraY = goon.y - 350;
                }
                platforms.removeIf( p -> p.y - cameraY > 600); // supprimer les plateformes qui sont hors de l'écran
                while (platforms.size() < 11){
                    double x = rand.nextDouble() * (400 - Platform.WIDTH);
                    double y = cameraY - rand.nextDouble() * Gooner.h ; // générer des plateformes au-dessus de l'écran
                    platforms.add(new Platform(x, y));
                }

                draw(goon,platforms);

            }    
        };        
        timer.start();
    }


        public void handleKeyPress(KeyCode code) {
            if (code == KeyCode.LEFT) {
                goon.moveLeft();
            } else if (code == KeyCode.RIGHT) {
                goon.moveRight();
            } else if (code == KeyCode.SPACE) {
                goon.jump();
            }
            if (isGameOver) {
                if (code== KeyCode.SPACE){
                    resetGame();
                }
            }
        }
    
        public void handleKeyRelease(KeyCode code) {
            if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                goon.stopX();
            }
        }
    
    private void resetGame() {
        goon.x = standX;
        goon.y = standY;
        goon.velocityY = 0;
        cameraY = 0;
        platforms.clear();
        generatePlatform(platforms);
        isGameOver = false;
        scorePanel.reset();
    }

    
    public void draw(Gooner goon, List<Platform> platforms){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,400,600);
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(goon.x,goon.y - cameraY,goon.w,goon.h);
        for (Platform p : platforms){
            gc.setFill(Color.GRAY);
            gc.fillRect(p.x, p.y - cameraY, p.WIDTH, p.HEIGHT);
        }
    }

    public void generatePlatform(List<Platform> platforms){
    Random random = new Random();
    for (int i=0; i<11; i++){
        double x = random.nextDouble() * (400 - Platform.WIDTH); // x aléatoire
        double y = 500 - i * Gooner.h; // y décroissant pour espacer les plateformes
        platforms.add(new Platform(x, y));
        }
    }
}