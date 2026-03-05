package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import java.util.List;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;

public class GameView extends Pane {

    private Canvas canvas = new Canvas(400, 600);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private double standY = 280;
    private double standX = 180;
    Gooner goon = new Gooner(standX, standY);

    public GameView() {
        getChildren().add(canvas);

        
        
        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now){
                goon.update();
                draw(goon);
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
    
    

    
    public void draw(Gooner goon){
        goon.update();
        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,400,600);
        gc.setFill(Color.BLUE);
        gc.fillRect(goon.x,goon.y,40,60);
    }    
    
    
}