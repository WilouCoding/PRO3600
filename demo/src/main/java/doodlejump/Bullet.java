package doodlejump;
 
public class Bullet {
    public double x, y;
    public static final double WIDTH = 6;
    public static final double HEIGHT = 12;
    private static final double SPEED = 0.5;
    public boolean active = true;
 
    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
    }
 
    public void update() {
        y -= SPEED; // monte vers le haut
    }
}
 