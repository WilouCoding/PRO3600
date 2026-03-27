package doodlejump;
 
public class Monster {
    public double x, y;
    public static final double WIDTH = 40;
    public static final double HEIGHT = 30;
    private double velocityX;
    public boolean isDead = false;
 
    public Monster(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0.8; // se déplace horizontalement
    }
 
    public void update() {
        x += velocityX;
        // Rebondit sur les bords de l'écran
        if (x <= 0 || x + WIDTH >= 400) {
            velocityX = -velocityX;
        }
    }
}
 