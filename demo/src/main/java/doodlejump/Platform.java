package doodlejump;

import java.util.Random;

public class Platform {
    public double x;
    public double y;
    public static final double WIDTH = 70;
    public static final double HEIGHT = 12;

    public double velocityPlatx;
    public double velocityPlaty; 

    public Platform(double x, double y){
        this.x=x;
        this.y=y;
        velocityPlaty=0;
        velocityPlatx=0;
    }

}

