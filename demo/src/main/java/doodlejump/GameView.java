package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import java.util.List;

public class GameView extends Pane {
    private Canvas canvas = new Canvas(600, 600);
    //private GraphicsContext gc = canvas.getGraphicsContext2D();
    public GameView() {
        getChildren().add(canvas);
    }
    //public void draw(Player player, List<Platform> platforms) {
        // gc.setFill(Color.BLACK);

    //}
}