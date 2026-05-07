package doodlejump;

import java.util.Random;

public class Platform {
    public double x;
    public double y;
    public static final double WIDTH = 70;
    public static final double HEIGHT = 12;

    public double velocityPlatx;
    public double velocityPlaty; 

    public boolean isFragile = false; // Est-ce qu'elle se casse ?
    public int bounceCount = 0; // Combien de fois on a sauté dessus

    public Platform(double x, double y, boolean isFragile){
        this.x=x;
        this.y=y;
        velocityPlaty=0;
        velocityPlatx=0;
        this.isFragile = isFragile;
    }

}

