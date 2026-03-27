package doodlejump;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MainMenuView extends VBox {

    public MainMenuView(Runnable onPlayPressed) {
        this.setSpacing(20);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: black;");
        this.setPrefSize(400, 600);

        Label title = new Label("GOONER JUMP");
        title.setTextFill(Color.BLUEVIOLET);
        title.setFont(Font.font("Arial", 40));

        Button playButton = new Button("JOUER");
        playButton.setStyle("-fx-font-size: 20px; -fx-background-color: #7132cf; -fx-text-fill: white;");
        playButton.setOnAction(e -> onPlayPressed.run());

        // Bouton Boutique (pour tes futurs cosmétiques)
        Button shopButton = new Button("BOUTIQUE (Bientôt)");
        shopButton.setDisable(true); 

        this.getChildren().addAll(title, playButton, shopButton);
    }
}