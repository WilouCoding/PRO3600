package doodlejump;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class App extends Application {

    @Override          
    public void start(Stage stage) {

        GameView gameView = new GameView();
        Scene scene = new Scene(gameView, 400, 600);
        scene.setOnKeyPressed(e -> gameView.handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> gameView.handleKeyRelease(e.getCode()));
        stage.setScene(scene);
        stage.setTitle("DoodleJump");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}