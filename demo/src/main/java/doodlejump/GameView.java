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
    private List<Coin> coins = new ArrayList<>();
    private int platformCount = 0;
    
    Gooner goon = new Gooner(standX, standY);
    
    private double cameraY = 0;
    private Random rand = new Random();
    
    public GameView() {
        getChildren().add(canvas);
        generatePlatform(platforms);
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now){
                
                goon.update();
                
                if (goon.velocityY > 0){
                    for (Platform p : platforms){ // collision
                        if (goon.x < p.x + p.WIDTH 
                            && goon.x + Gooner.w > p.x 
                            && goon.y + Gooner.h >= p.y
                            && goon.y + Gooner.h <= p.y + p.HEIGHT){
                                goon.jump();
                        }
                    }

                }
                if (goon.y < cameraY + 350){
                    cameraY = goon.y - 350;
                }
                for (Coin c : coins) {
                    if (!c.collected && goon.x < c.x + Coin.SIZE && goon.x + Gooner.w > c.x && goon.y < c.y + Coin.SIZE && goon.y + Gooner.h > c.y) {
                        c.collected = true;
                        goon.coins++;
                    }
                }
                coins.removeIf(c -> c.collected);


                platforms.removeIf( p -> p.y - cameraY > 600); // supprimer les plateformes qui sont hors de l'écran
                coins.removeIf(c -> c.y - cameraY > 600); // supprimer les pièces qui sont hors de l'écran
                while (platforms.size() < 11){
                    double x = rand.nextDouble() * (400 - Platform.WIDTH);
                    double y = cameraY - rand.nextDouble() * 300 ; // générer des plateformes au-dessus de l'écran
                    platforms.add(new Platform(x, y));
                    platformCount++;

                    if (platformCount % 20  == 0){ // toutes les 5 plateformes, ajouter une pièce
                        coins.add(new Coin(x + Platform.WIDTH / 2 - Coin.SIZE / 2, y - Coin.SIZE));
                    }
                }

                draw(goon,platforms,coins);

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
        }
    
        public void handleKeyRelease(KeyCode code) {
            if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                goon.stopX();
            }
        }
    
    

    
    public void draw(Gooner goon, List<Platform> platforms, List<Coin> coins) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,400,600);
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(goon.x,goon.y - cameraY,goon.w,goon.h);
        for (Platform p : platforms){
            gc.setFill(Color.GRAY);
            gc.fillRect(p.x, p.y - cameraY, p.WIDTH, p.HEIGHT);
        }
        gc.setFill(Color.YELLOW);
        for (Coin c : coins) {
            gc.fillOval(c.x, c.y - cameraY, Coin.SIZE, Coin.SIZE);
        }
        gc.setFill(Color.WHITE);
        gc.fillText("Pièces : " + goon.coins, 10, 20);
        }
    

    public void generatePlatform(List<Platform> platforms){
    Random random = new Random();
    platforms.add(new Platform(standX, standY + Gooner.h)); // Plateforme de départ
    for (int i=0; i<11; i++){
        
        double x = random.nextDouble() * (400 - Platform.WIDTH); // x aléatoire
        double y = 500 - i * Gooner.h; // y décroissant pour espacer les plateformes
        platforms.add(new Platform(x, y));
        }
    }
}
