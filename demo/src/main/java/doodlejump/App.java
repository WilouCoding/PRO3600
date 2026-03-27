package doodlejump;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private StackPane root; // Conteneur principal

    @Override          
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.root = new StackPane();
        
        Scene scene = new Scene(root, 400, 600);
        
        // On affiche le menu au démarrage
        showMenu();

        stage.setScene(scene);
        stage.setTitle("GoonerJump");
        stage.show();
    }

    public void showMenu() {
        MainMenuView menu = new MainMenuView(() -> startGame());
        root.getChildren().setAll(menu);
    }

    public void startGame() {
        GameView gameView = new GameView(this); // On passe 'this' pour pouvoir revenir au menu
        root.getChildren().setAll(gameView);
        
        // On redonne le focus au clavier pour le jeu
        gameView.setFocusTraversable(true);
        gameView.requestFocus();
        
        // Gestion des touches
        primaryStage.getScene().setOnKeyPressed(e -> gameView.handleKeyPress(e.getCode()));
        primaryStage.getScene().setOnKeyReleased(e -> gameView.handleKeyRelease(e.getCode()));
    }

    public static void main(String[] args) {
        launch();
    }
}