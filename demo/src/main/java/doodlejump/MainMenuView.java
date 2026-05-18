package doodlejump;

import java.util.function.Supplier;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainMenuView extends VBox {

    public MainMenuView(Runnable onPlayPressed, Runnable onShopPressed, Runnable onAccountPressed, Supplier<String> currentUserSupplier) {
        this.setSpacing(16);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: black;");
        this.setPrefSize(400, 600);

        Label title = new Label("GOONER JUMP");
        title.setTextFill(Color.BLUEVIOLET);
        title.setFont(Font.font("Arial", 40));

        Label accountLabel = new Label();
        accountLabel.setTextFill(Color.LIGHTGRAY);
        accountLabel.setFont(Font.font("Arial", 16));
        String currentUser = currentUserSupplier.get();
        if (currentUser == null || currentUser.isBlank()) {
            accountLabel.setText("Aucun compte connecté");
        } else {
            accountLabel.setText("Connecté : " + currentUser);
        }

        Button playButton = new Button("JOUER");
        playButton.setStyle("-fx-font-size: 20px; -fx-background-color: #7132cf; -fx-text-fill: white;");
        playButton.setOnAction(e -> onPlayPressed.run());

        Button accountButton = new Button("COMPTES");
        accountButton.setStyle("-fx-font-size: 18px; -fx-background-color: #4a90e2; -fx-text-fill: white;");
        accountButton.setOnAction(e -> onAccountPressed.run());

        Button shopButton = new Button("BOUTIQUE");
        shopButton.setStyle("-fx-font-size: 18px; -fx-background-color: #33a02c; -fx-text-fill: white;");
        shopButton.setOnAction(e -> onShopPressed.run());

        this.getChildren().addAll(title, accountLabel, playButton, accountButton, shopButton);
    }
}