package doodlejump;

public class Coin {
    public double x;
    public double y;
    public static final double SIZE = 50;
    public boolean collected = false; // Pour savoir si la pièce a été collectée

    public Coin(double x, double y){
        this.x=x;
        this.y=y;
    }

}
