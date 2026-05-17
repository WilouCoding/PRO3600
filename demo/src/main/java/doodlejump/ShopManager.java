package doodlejump;

import java.io.*;
import java.util.*;

public class ShopManager {
    private static final String FILE_NAME = "shop.txt";
    private List<ShopItem> items = new ArrayList<>();

    public ShopManager() {
        initItems();
        loadPurchases();
    }

    private void initItems() {
        // Skins
        items.add(new ShopItem("skin_default", "Skin Par défaut", 0,   "skin",  "#e0b998"));
        items.add(new ShopItem("skin_bambino",    "Bambino",     5,  "skin",  "#000000"));
        items.add(new ShopItem("skin_red",    "Skin Rouge",   50,  "skin",  "#e74c3c"));
        items.add(new ShopItem("skin_green",  "Skin Vert",    50,  "skin",  "#2ecc71"));
        items.add(new ShopItem("skin_gold",   "Skin Or",      200, "skin",  "#f1c40f"));
        items.add(new ShopItem("skin_legend","Skin Legend", 500, "skin",  "#9b59b6"));

        // Powers 
        items.add(new ShopItem("power_shield", "Bouclier",    100, "power", "#3498db"));
        items.add(new ShopItem("power_magnet", "Aimant",      150, "power", "#f39c12"));
    }

    public List<ShopItem> getItems() { return items; }

    public ShopItem getEquippedSkin() {
        return items.stream()
            .filter(i -> i.type.equals("skin") && i.equipped)
            .findFirst().orElse(null);
    }

    public boolean buy(ShopItem item, CoinManager coinManager) {
        if (item.owned || coinManager.getCoins() < item.price) return false;
        coinManager.addCoins(-item.price);
        item.owned = true;
        savePurchases();
        return true;
    }

    public void equip(ShopItem item) {
        if (!item.owned) return;
        // Déséquipe tous les items du même type
        items.stream()
            .filter(i -> i.type.equals(item.type))
            .forEach(i -> i.equipped = false);
        item.equipped = true;
        savePurchases();
    }

    private void savePurchases() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME));
            for (ShopItem item : items) {
                writer.println(item.id + "," + item.owned + "," + item.equipped);
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Erreur sauvegarde boutique.");
        }
    }

    private void loadPurchases() {
        try {
            File file = new File(FILE_NAME);
            if (!file.exists()) return;
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length < 3) continue;
                items.stream()
                    .filter(i -> i.id.equals(parts[0]))
                    .findFirst()
                    .ifPresent(i -> {
                        i.owned   = Boolean.parseBoolean(parts[1]);
                        i.equipped = Boolean.parseBoolean(parts[2]);
                    });
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Erreur chargement boutique.");
        }
    }
}