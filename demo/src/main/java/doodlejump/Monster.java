package doodlejump;

import javafx.scene.image.Image;

public class Monster {
    public double x, y;
    public static final double WIDTH = 40;
    public static final double HEIGHT = 30;
    private double velocityX;
    public boolean isDead = false;

    public Image skin;
 
    public Monster(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0.8; // se déplace horizontalement
        // Chargement de l'image
        try {
            this.skin = new Image(getClass().getResourceAsStream("/monster_skin.png"));
        } catch (Exception e) {
            System.out.println("Erreur : Image du monstre introuvable !");
        }
    }
 
    public void update() {
        x += velocityX;
        // Rebondit sur les bords de l'écran
        if (x <= 0 || x + WIDTH >= 400) {
            velocityX = -velocityX;
        }
    }
}
 