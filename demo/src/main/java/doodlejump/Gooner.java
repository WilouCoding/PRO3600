package doodlejump;

public class Gooner {
    public double x;
    public double y;
    public double velocityY;
    public double velocityX;  // vitesse du perso
    public double GRAVITY = 0.0004;  // gravité
    public static final double w = 40; 
    public static final double h =60;  // valeurs fixe pour la taille du persp
    public Gooner(double x, double y){
        this.x=x;
        this.y=y;
        velocityY=0;  
        velocityX=0;  // car le perso n'a pas de vitesse de base
    }
    
    public void update() {
    velocityY += GRAVITY;
    y += velocityY;
    x += velocityX;

    // On attend que le perso soit entièrement sorti pour réinitialiser sa position réelle
    if (x > 400) {
        x -= 400; 
    } else if (x < -Gooner.w) {
        x += 400;
    }
}

    public void jump(){
        velocityY=-0.3;
    }

    public void moveLeft(){
        velocityX-=0.08;
    }
    
    public void moveRight(){
        velocityX+=0.08;
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