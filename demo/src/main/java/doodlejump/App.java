package doodlejump;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private StackPane root; // Conteneur principal
    private CoinManager coinManager = new CoinManager(); // Gestionnaire de pièces
    private ShopManager shopManager = new ShopManager(); // Gestionnaire de la boutique
    private AccountManager accountManager = new AccountManager();
    private String currentPlayerUsername = null;

    public void start(Stage stage) {
        this.primaryStage = stage;
        this.root = new StackPane();
        
        Scene scene = new Scene(root, 400, 600);
        
        // On affiche la page de compte au démarrage
        showAccountMenu();

        stage.setScene(scene);
        stage.setTitle("GoonerJump");
        stage.show();
    }

    public void showMenu() {
        MainMenuView menu = new MainMenuView(this::startGame, this::showShop, this::showAccountMenu, () -> currentPlayerUsername);
        root.getChildren().setAll(menu);
    }

    public void startGame() {
        GameView gameView = new GameView(this, coinManager, shopManager, currentPlayerUsername, accountManager); // On passe le compte courant si existant
        root.getChildren().setAll(gameView);
        
        // On redonne le focus au clavier pour le jeu
        gameView.setFocusTraversable(true);
        gameView.requestFocus();
        
        // Gestion des touches
        primaryStage.getScene().setOnKeyPressed(e -> gameView.handleKeyPress(e.getCode()));
        primaryStage.getScene().setOnKeyReleased(e -> gameView.handleKeyRelease(e.getCode()));
    }

    public void showAccountMenu() {
        AccountMenuView accountMenu = new AccountMenuView(accountManager, new AccountMenuView.AccountMenuCallback() {
            @Override
            public void onLoggedIn(String username) {
                currentPlayerUsername = username;
                showMenu();
            }

            @Override
            public void onBack() {
                showMenu();
            }
        });
        root.getChildren().setAll(accountMenu);
    }

    public static void main(String[] args) {
        launch();
    }

    public void showShop() {
        ShopView shopView = new ShopView(this, coinManager, shopManager);
        root.getChildren().setAll(shopView);
    }
}