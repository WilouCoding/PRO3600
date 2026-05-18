package doodlejump;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class AccountMenuView extends VBox {
    public AccountMenuView(AccountManager accountManager, AccountMenuCallback callback) {
        setSpacing(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: black;");
        setPrefSize(400, 600);

        Label title = new Label("COMPTES");
        title.setTextFill(Color.CORNFLOWERBLUE);
        title.setFont(Font.font("Arial", 36));

        Label statusLabel = new Label("Connexion ou création de compte");
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setFont(Font.font("Arial", 14));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.setMaxWidth(260);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(260);

        Button loginButton = new Button("Se connecter");
        Button signupButton = new Button("Créer un compte joueur");
        Button guestButton = new Button("Jouer en invité");

        String buttonStyle = "-fx-font-size: 16px; -fx-background-color: #7132cf; -fx-text-fill: white; -fx-cursor: hand;";
        loginButton.setStyle(buttonStyle);
        signupButton.setStyle(buttonStyle);
        guestButton.setStyle(buttonStyle);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Veuillez saisir nom d'utilisateur et mot de passe.");
                return;
            }
            if (accountManager.authenticate(username, password)) {
                statusLabel.setText("Connecté en tant que " + username);
                callback.onLoggedIn(username);
            } else {
                statusLabel.setText("Nom d'utilisateur ou mot de passe incorrect.");
            }
        });

        signupButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Veuillez saisir nom d'utilisateur et mot de passe.");
                return;
            }
            if (accountManager.createPlayerAccount(username, password)) {
                statusLabel.setText("Compte créé pour " + username + ". Connecté.");
                callback.onLoggedIn(username);
            } else {
                statusLabel.setText("Ce nom d'utilisateur existe déjà.");
            }
        });

        guestButton.setOnAction(e -> callback.onLoggedIn(null));

        getChildren().addAll(title, statusLabel, usernameField, passwordField, loginButton, signupButton, guestButton);
    }

    public interface AccountMenuCallback {
        void onLoggedIn(String username);
        void onBack();
    }
}
