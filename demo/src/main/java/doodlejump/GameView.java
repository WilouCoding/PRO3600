package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import java.util.ArrayList;
import java.util.*;

public class GameView extends Pane {

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

                if (goon.velocityY > 0){
                    for (Platform p : platforms){
                        if (goon.x < p.x + p.WIDTH 
                            && goon.x + Gooner.w > p.x 
                            && goon.y + Gooner.h >= p.y
                            && goon.y + Gooner.h <= p.y + p.HEIGHT){
                            goon.jump();
                        }
                    }

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
        }
    
        public void handleKeyRelease(KeyCode code) {
            if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                goon.stopX();
            }
        }
    
    

    
    public void draw(Gooner goon, List<Platform> platforms){
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,400,600);
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(goon.x,goon.y,goon.w,goon.h);
        for (Platform p : platforms){
            gc.setFill(Color.GRAY);
            gc.fillRect(p.x, p.y, p.WIDTH, p.HEIGHT);
        }
    }

    public void generatePlatform(List<Platform> platforms){
    Random random = new Random();
    for (int i=0; i<10; i++){
        double x = random.nextDouble() * (400 - Platform.WIDTH); // x aléatoire
        double y = 500 - i * Gooner.h; // y décroissant pour espacer les plateformes
        platforms.add(new Platform(x, y));
        }
    }
}