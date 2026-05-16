package doodlejump;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class AccountMenuView extends VBox {
    private final Label messageLabel = new Label();

    public AccountMenuView(AccountManager accountManager, Runnable onBack, java.util.function.Consumer<String> onLoginSuccess) {
        setSpacing(12);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: black;");
        setPrefSize(400, 600);

        Label title = new Label("GESTION DES COMPTES");
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        usernameField.setMaxWidth(260);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        passwordField.setMaxWidth(260);

        TextField emailField = new TextField();
        emailField.setPromptText("Email (pour compte utilisateur)");
        emailField.setMaxWidth(260);

        Button createPlayerButton = new Button("Créer compte joueur");
        Button createUserButton = new Button("Créer compte utilisateur");
        Button loginButton = new Button("Se connecter");
        Button backButton = new Button("Retour");

        createPlayerButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (username.isBlank() || password.isBlank()) {
                showMessage("Veuillez saisir un nom et un mot de passe.", Color.ORANGE);
                return;
            }
            if (accountManager.createPlayerAccount(username, password)) {
                showMessage("Compte joueur créé avec succès !", Color.LIGHTGREEN);
            } else {
                showMessage("Ce nom est déjà utilisé.", Color.ORANGERED);
            }
        });

        createUserButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String email = emailField.getText().trim();
            if (username.isBlank() || password.isBlank() || email.isBlank()) {
                showMessage("Renseignez nom, mot de passe et email.", Color.ORANGE);
                return;
            }
            if (accountManager.createUserAccount(username, password, email, "user")) {
                showMessage("Compte utilisateur créé !", Color.LIGHTGREEN);
            } else {
                showMessage("Ce nom est déjà utilisé.", Color.ORANGERED);
            }
        });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            if (accountManager.authenticate(username, password)) {
                onLoginSuccess.accept(username);
                showMessage("Connexion réussie : " + username, Color.LIGHTGREEN);
            } else {
                showMessage("Nom ou mot de passe incorrect.", Color.ORANGERED);
            }
        });

        backButton.setOnAction(e -> onBack.run());

        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);

        getChildren().addAll(title, usernameField, passwordField, emailField,
                createPlayerButton, createUserButton, loginButton, messageLabel, backButton);
    }

    private void showMessage(String text, Color color) {
        messageLabel.setTextFill(color);
        messageLabel.setText(text);
    }
}
