package doodlejump;

import javafx.scene.image.Image;

public class Gooner {
    public double x;
    public double y;
    public double velocityY;
    public double velocityX;
    public static final double GRAVITY = 0.06;
    public static final double MAX_SPEED_X = 6.0;
    public static final double w = 50;
    public static final double h = 60;
    public int coins = 0;

    public Image skin;
    public boolean facingLeft = false; // Pour savoir dans quelle direction le personnage regarde

    public Gooner(double x, double y){
        this(x, y, "/gooner_skin.png");
    }

    public Gooner(double x, double y, String skinResource){
        this.x = x;
        this.y = y;
        velocityY = 0;
        velocityX = 0;
        setSkin(skinResource);
    }

    public void setSkin(String skinResource) {
        if (skinResource == null) return;
        try {
            this.skin = new Image(getClass().getResourceAsStream(skinResource));
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du skin : " + e.getMessage());
        }
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
        velocityX = Math.max(velocityX - 2, -MAX_SPEED_X);
        facingLeft = true;
    }
    
    public void moveRight(){
        velocityX = Math.min(velocityX + 2, MAX_SPEED_X);
        facingLeft = false;
    }
    
    public void stopX(){
        velocityX = 0;
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