package doodlejump;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
public class App extends Application {

    @Override          
    public void start(Stage stage) {
        GameView gameView = new GameView();
        Scene scene = new Scene(gameView, 600, 600);

        stage.setScene(scene);
        stage.setTitle("DoodleJump");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}