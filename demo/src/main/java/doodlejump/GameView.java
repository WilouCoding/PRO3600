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

    public GameView() {
        getChildren().add(canvas);
        generatePlatform(platforms);
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now){
                
                goon.update();

                if (goon.velocityY > 0){  // permet de gérer les collisions du perso
                    for (Platform p : platforms){
                        if (goon.x < p.x + p.WIDTH 
                            && goon.x + Gooner.w > p.x 
                            && goon.y + Gooner.h >= p.y
                            && goon.y + Gooner.h <= p.y + p.HEIGHT){
                            goon.jump();
                        }
                    }

                }
                draw(goon,platforms);  // dessine le perso et les plateformes

            }    
        };        
        timer.start();
    }


        public void handleKeyPress(KeyCode code) {  // Associe les touches aux fonctions qu'elles doivent remplir
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
    
    public void draw(Gooner goon, List<Platform> platforms){  
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,400,600);  // modifie le fond de l'image 
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(goon.x,goon.y,goon.w,goon.h);  // modifie la couleur du perso
        //Si le perso dépasse à droite, on dessine une copie à gauche
        if (goon.x + Gooner.w > 400) {
        gc.fillRect(goon.x - 400, goon.y, Gooner.w, Gooner.h);
        }
        // Si le perso dépasse à gauche, on dessine une copie à droite
        else if (goon.x < 0) {
        gc.fillRect(goon.x + 400, goon.y, Gooner.w, Gooner.h);
        }
        for (Platform p : platforms){
            gc.setFill(Color.GRAY);
            gc.fillRect(p.x, p.y, p.WIDTH, p.HEIGHT);  // modifie la couleur des plateformes
        }
        if (isGameOver) { // Message de fin
            gc.setFill(Color.RED);
            gc.setFont(new javafx.scene.text.Font("Arial", 40));
            gc.fillText("GAME OVER", 100, 300);

            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial", 20));
            gc.fillText("Appuyez sur ESPACE pour recommencer", 60, 350);
        }
    }

    public void handle(long now) { 
            if (isGameOver) {
                draw(goon, platforms); // On continue de dessiner l'écran de fin
                return;
            }
            goon.update();
            // Si le perso descend plus bas que la limite de l'écran (600)
            if (goon.y > 600) {
                isGameOver = true;
                System.out.println("Partie terminée !");
            }
            if (goon.velocityY > 0) {
                for (Platform p : platforms) {
                    if (goon.x < p.x + Platform.WIDTH 
                        && goon.x + Gooner.w > p.x 
                        && goon.y + Gooner.h >= p.y
                        && goon.y + Gooner.h <= p.y + Platform.HEIGHT) {
                        goon.jump();
                    }
                }
            }
            draw(goon, platforms); // on redessine à chaque image
        }

    public void generatePlatform(List<Platform> platforms){  // génère les plateformes de manière aléatoire
    Random random = new Random();
    for (int i=0; i<10; i++){
        double x = random.nextDouble() * (400 - Platform.WIDTH);  // x aléatoire
        double y = 500 - i * Gooner.h;  // y décroissant pour espacer les plateformes
        platforms.add(new Platform(x, y));
        }
    }

    
}