package doodlejump;

public class Gooner {
    public double x;
    public double y;
    public double velocityY;
    public double velocityX;
    public static final double GRAVITY = 0.06;
    public static final double w = 40;
    public static final double h =60;
    public Gooner(double x, double y){
        this.x=x;
        this.y=y;
        velocityY=0;
        velocityX=0;
    }
    public void update(){
        velocityY+=GRAVITY;
        y+=velocityY;
        x+=velocityX;

        if (x>400){
            x-=400;
        }
        if (x< -Gooner.w){
            x+=400;
        }
    }

    public void jump(){
        velocityY=-6.0;
    }

    public void moveLeft(){
        velocityX-=0.8;
    }
    
    public void moveRight(){
        velocityX+=0.8;
    }
    
    public void stopX(){
        velocityX=0;
    }
    /*public double getx(){
        return x;
    }
    public double gety(){
        return y;
    }  
    public double getVelocityY(){
        return velocityY;
    }*/
}