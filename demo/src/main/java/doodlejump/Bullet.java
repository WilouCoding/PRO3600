package doodlejump;
 
public class Bullet {
    public double x, y;
    public static final double WIDTH = 8;
    public static final double HEIGHT = 8;
    private static final double SPEED = 10.0;
    public boolean active = true;
 
    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
    }
 
    public void update() {
        y -= SPEED; // monte vers le haut
    }
}
 