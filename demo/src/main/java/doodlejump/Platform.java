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
    public boolean isMoving; //est-ce qu'elle bouge ?
    public boolean isGhost; // plateforme fantome (invisible)
    public double velocityX = 1.0; // Vitesse de déplacement (à ajuster)

    public Platform(double x, double y, boolean isFragile, boolean isMoving, boolean isGhost) {
        this.x=x;
        this.y=y;
        velocityPlaty=0;
        velocityPlatx=0;
        this.isFragile = isFragile;
        this.isGhost = isGhost;
        this.isMoving = isMoving;

        // On donne une direction aléatoire au départ (gauche ou droite)
        if (isMoving && Math.random() > 0.5) {
            this.velocityX *= -1;
        }
    }

    // Méthode pour mettre à jour la position
    public void update() {
        if (isMoving) {
            x += velocityX;
            // Rebondir sur les bords de l'écran (400)
            if (x <= 0 || x + WIDTH >= 400) {
                velocityX *= -1; // On change de direction
            }
        }
    }

}

