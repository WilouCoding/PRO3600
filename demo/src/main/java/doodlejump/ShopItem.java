package doodlejump;

public class ShopItem {
    public String id;
    public String name;
    public int price;
    public boolean owned;
    public boolean equipped;
    public String type; // "skin" ou "power"
    public String color;
    public String skinResource;

    public ShopItem(String id, String name, int price, String type, String color, String skinResource) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
        this.color = color;
        this.skinResource = skinResource;
        this.owned = false;
        this.equipped = false;
    }
}