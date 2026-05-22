package doodlejump;

import javafx.scene.image.Image;

enum BonusType {
    HAT,
    TRAMPOLINE
}

public class Bonus {
    public double x, y;
    public static final double WIDTH = 25;
    public static final double HEIGHT = 25;
    public boolean collected = false;
    public Image skin;
    public BonusType type;
    public double width = WIDTH;
    public double height = HEIGHT;
    
    // On garde une référence à la plateforme sur laquelle il est posé
    public Platform platform; 

    public Bonus(Platform p, BonusType type) {
        this.platform = p;
        this.type = type;
        this.width = type == BonusType.TRAMPOLINE ? 40 : WIDTH;
        this.height = type == BonusType.TRAMPOLINE ? 35 : HEIGHT;

        // On centre le bonus sur la plateforme en X, et on le pose juste au-dessus en Y
        this.x = p.x + (Platform.WIDTH / 2) - (this.width / 2);
        this.y = p.y - this.height;
        
        try {
            if (type == BonusType.HAT) {
                this.skin = new Image(getClass().getResourceAsStream("/chapeau.png"));
            } else {
                this.skin = new Image(getClass().getResourceAsStream("/trampoline.png"));
            }
        } catch (Exception e) {
            System.out.println("Erreur : Image du bonus introuvable !");
            this.skin = null;
        }
    }

    public void update() {
        // Si la plateforme bouge, le bonus doit la suivre !
        this.x = platform.x + (Platform.WIDTH / 2) - (this.width / 2);
        this.y = platform.y - this.height;
    }

}
