package doodlejump;

public class Coin {
    public double x;
    public double y;
    public static final double SIZE = 15;
    public boolean collected = false;

    public Coin(double x, double y) {
        this.x = x;
        this.y = y;
    }
}