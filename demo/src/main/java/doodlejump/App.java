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
    private final AccountManager accountManager = new AccountManager();
    private String currentPlayerUsername = null;

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
        MainMenuView menu = new MainMenuView(() -> startGame(), this::showShop, this::showAccountMenu);
        root.getChildren().setAll(menu);
    }

    public void showAccountMenu() {
        AccountMenuView accountMenu = new AccountMenuView(accountManager, this::showMenu, this::setCurrentPlayerUsername);
        root.getChildren().setAll(accountMenu);
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

    public void setCurrentPlayerUsername(String username) {
        this.currentPlayerUsername = username;
    }

    public String getCurrentPlayerUsername() {
        return currentPlayerUsername;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public CoinManager getCoinManager() {
        return coinManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public static void main(String[] args) {
        launch();
    }

    public void showShop() {
        ShopView shopView = new ShopView(this, coinManager, shopManager);
        root.getChildren().setAll(shopView);
    }
}