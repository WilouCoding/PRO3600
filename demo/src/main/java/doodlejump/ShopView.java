package doodlejump;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ShopView extends VBox {
    private final AccountManager accountManager;
    private final String currentPlayerUsername;

    public ShopView(App app, CoinManager coinManager, ShopManager shopManager, AccountManager accountManager, String currentPlayerUsername) {
        this.accountManager = accountManager;
        this.currentPlayerUsername = currentPlayerUsername;
        this.setSpacing(10);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #0a0a1a;");
        this.setPrefSize(400, 600);

        // Header
        Label title = new Label("BOUTIQUE");
        title.setTextFill(Color.GOLD);
        title.setFont(Font.font("Arial", 35));

        Label balance = new Label("🪙 " + coinManager.getCoins() + " pièces");
        balance.setTextFill(Color.WHITE);
        balance.setFont(Font.font("Arial", 18));

        // Onglets Skins / Powers
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab skinTab  = new Tab("Skins",  buildShopContent(shopManager, coinManager, "skin",  balance));
        Tab powerTab = new Tab("Pouvoirs", buildShopContent(shopManager, coinManager, "power", balance));
        tabPane.getTabs().addAll(skinTab, powerTab);

        // Bouton retour
        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #7132cf; -fx-text-fill: white;");
        backBtn.setOnAction(e -> app.showMenu());

        this.getChildren().addAll(title, balance, tabPane, backBtn);
    }

    private VBox buildShopContent(ShopManager shopManager, CoinManager coinManager, String type, Label balance) {
        ObjectProperty<ShopItem> selectedItem = new SimpleObjectProperty<>();

        VBox content = new VBox(10);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: transparent;");

        HBox previewRow = new HBox(14);
        previewRow.setAlignment(Pos.CENTER_LEFT);
        previewRow.setPrefHeight(220);
        previewRow.setMinHeight(220);
        previewRow.setMaxWidth(380);
        previewRow.setStyle("-fx-background-color: #1a1a3a; -fx-padding: 18; -fx-background-radius: 14;");

        VBox previewText = new VBox(10);
        previewText.setAlignment(Pos.TOP_LEFT);

        Label previewTitle = new Label("Sélectionnez un item");
        previewTitle.setTextFill(Color.GOLD);
        previewTitle.setFont(Font.font("Arial", 22));

        Label previewType = new Label("");
        previewType.setTextFill(Color.LIGHTGRAY);
        previewType.setFont(Font.font("Arial", 14));

        Label previewDescription = new Label("");
        previewDescription.setTextFill(Color.WHITE);
        previewDescription.setWrapText(true);
        previewDescription.setMaxWidth(240);
        previewDescription.setFont(Font.font("Arial", 13));

        Label previewPrice = new Label("");
        previewPrice.setTextFill(Color.GOLD);
        previewPrice.setFont(Font.font("Arial", 16));

        Button previewAction = new Button("Sélectionner");
        previewAction.setStyle("-fx-background-color: #7132cf; -fx-text-fill: white; -fx-font-size: 14px;");

        previewText.getChildren().addAll(previewTitle, previewType, previewDescription, previewPrice, previewAction);

        Region previewIcon = new Region();
        previewIcon.setPrefSize(120, 120);
        previewIcon.setStyle("-fx-background-color: #7132cf; -fx-background-radius: 18;");

        previewRow.getChildren().addAll(previewIcon, previewText);

        GridPane itemGrid = new GridPane();
        itemGrid.setHgap(10);
        itemGrid.setVgap(10);
        itemGrid.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(itemGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefViewportHeight(340);

        final Runnable[] refreshGrid = new Runnable[1];

        previewAction.setOnAction(e -> {
            ShopItem item = selectedItem.get();
            if (item == null) return;
            if (!item.owned) {
                boolean success = shopManager.buy(item, coinManager);
                if (success) {
                    balance.setText("🪙 " + coinManager.getCoins() + " pièces");
                    updatePreview(item, previewTitle, previewType, previewDescription, previewPrice, previewAction, previewIcon);
                    refreshGrid[0].run();
                }
            } else {
                shopManager.equip(item);
                if (currentPlayerUsername != null && "skin".equals(item.type)) {
                    accountManager.setPlayerEquippedSkin(currentPlayerUsername, item.id);
                }
                updatePreview(item, previewTitle, previewType, previewDescription, previewPrice, previewAction, previewIcon);
                refreshGrid[0].run();
            }
        });

        refreshGrid[0] = () -> {
            itemGrid.getChildren().clear();

            int column = 0;
            int row = 0;
            for (ShopItem item : shopManager.getItems()) {
                if (!item.type.equals(type)) continue;

                VBox card = new VBox(6);
                card.setPrefWidth(180);
                card.setPrefHeight(160);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: #222240; -fx-background-radius: 14; -fx-border-color: #444; -fx-border-radius: 14;");

                Label name = new Label(item.name);
                name.setTextFill(Color.WHITE);
                name.setFont(Font.font("Arial", 14));
                name.setWrapText(true);

                Region icon = new Region();
                icon.setPrefSize(100, 70);
                icon.setStyle(getItemPreviewStyle(item));

                Label price = new Label("🪙 " + item.price);
                price.setTextFill(Color.GOLD);
                price.setFont(Font.font("Arial", 14));

                Button actionBtn = new Button(item.owned ? (item.equipped ? "Équipé ✓" : "Équiper") : "Acheter");
                actionBtn.setPrefWidth(160);
                actionBtn.setStyle(item.equipped
                    ? "-fx-background-color: #2ecc71; -fx-text-fill: white;"
                    : "-fx-background-color: #7132cf; -fx-text-fill: white;");

                actionBtn.setOnAction(e -> {
                    selectedItem.set(item);
                    if (!item.owned) {
                        boolean success = shopManager.buy(item, coinManager);
                        if (success) {
                            balance.setText("🪙 " + coinManager.getCoins() + " pièces");
                        }
                    } else {
                        shopManager.equip(item);
                        if (currentPlayerUsername != null && "skin".equals(item.type)) {
                            accountManager.setPlayerEquippedSkin(currentPlayerUsername, item.id);
                        }
                    }
                });

                card.getChildren().addAll(icon, name, price, actionBtn);
                itemGrid.add(card, column, row);
                column++;
                if (column > 1) {
                    column = 0;
                    row++;
                }

                if (selectedItem.get() == null) {
                    selectedItem.set(item);
                    updatePreview(item, previewTitle, previewType, previewDescription, previewPrice, previewAction, previewIcon);
                }
            }
        };

        refreshGrid[0].run();

        content.getChildren().addAll(previewRow, scrollPane);
        return content;
    }

    private void updatePreview(ShopItem item, Label previewTitle, Label previewType, Label previewDescription, Label previewPrice, Button previewAction, Region previewIcon) {
        previewTitle.setText(item.name);
        previewType.setText(item.type.equals("skin") ? "Skin" : "Pouvoir");
        previewDescription.setText(item.type.equals("skin")
            ? "Change votre skin pour ce style unique dans le jeu."
            : "Active un pouvoir spécial qui améliore ton gameplay.");
        previewPrice.setText(item.owned ? (item.equipped ? "Équipé" : "Déjà acheté") : "Prix : 🪙 " + item.price);
        previewAction.setText(item.owned ? (item.equipped ? "Équipé ✓" : "Équiper") : "Acheter");
        previewAction.setStyle(item.equipped
            ? "-fx-background-color: #2ecc71; -fx-text-fill: white;"
            : "-fx-background-color: #7132cf; -fx-text-fill: white;");
        previewIcon.setStyle(getItemPreviewStyle(item));
    }

    private String getItemPreviewStyle(ShopItem item) {
        String color = item.color != null ? item.color : "#6f42c1";
        return "-fx-background-color: " + color + "; -fx-background-radius: 14;";
    }

    private VBox buildItemList(ShopManager shopManager, CoinManager coinManager, String type, Label balance) {
        VBox list = new VBox(10);
        list.setAlignment(Pos.TOP_CENTER);
        list.setStyle("-fx-padding: 10;");

        for (ShopItem item : shopManager.getItems()) {
            if (!item.type.equals(type)) continue;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #1a1a3a; -fx-padding: 10; -fx-background-radius: 8;");

            Label name  = new Label(item.name);
            name.setTextFill(Color.WHITE);
            name.setFont(Font.font("Arial", 16));
            name.setPrefWidth(160);

            Label price = new Label("🪙 " + item.price);
            price.setTextFill(Color.GOLD);
            price.setFont(Font.font("Arial", 14));
            price.setPrefWidth(70);

            Button actionBtn = new Button(
                item.owned ? (item.equipped ? "Équipé ✓" : "Équiper") : "Acheter"
            );
            actionBtn.setStyle(
                item.equipped
                    ? "-fx-background-color: #2ecc71; -fx-text-fill: white;"
                    : "-fx-background-color: #7132cf; -fx-text-fill: white;"
            );

            actionBtn.setOnAction(e -> {
                if (!item.owned) {
                    boolean success = shopManager.buy(item, coinManager);
                    if (success) {
                        actionBtn.setText("Équiper");
                        balance.setText("🪙 " + coinManager.getCoins() + " pièces");
                    }
                } else {
                    shopManager.equip(item);
                    actionBtn.setText("Équipé ✓");
                    actionBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                }
            });

            row.getChildren().addAll(name, price, actionBtn);
            list.getChildren().add(row);
        }
        return list;
    }
}