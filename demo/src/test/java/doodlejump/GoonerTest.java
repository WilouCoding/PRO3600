package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GoonerTest {
    private Gooner gooner;

    @BeforeEach
    public void setUp() {
        gooner = new Gooner(200, 300);
    }

    @Test
    public void testInitialPosition() {
        assertEquals(200, gooner.x, "Position X devrait être 200");
        assertEquals(300, gooner.y, "Position Y devrait être 300");
    }

    @Test
    public void testInitialVelocity() {
        assertEquals(0, gooner.velocityY, "Vitesse Y initiale devrait être 0");
        assertEquals(0, gooner.velocityX, "Vitesse X initiale devrait être 0");
    }

    @Test
    public void testGravityApplication() {
        double initialVelocityY = gooner.velocityY;
        gooner.update();
        
        assertEquals(initialVelocityY + Gooner.GRAVITY, gooner.velocityY, 0.001, 
                     "La gravité devrait augmenter la vitesse Y");
    }

    @Test
    public void testJump() {
        gooner.jump();
        
        assertEquals(-6.0, gooner.velocityY, "Le saut devrait donner une vélocité Y de -6.0");
    }

    @Test
    public void testMoveLeft() {
        double initialVelocityX = gooner.velocityX;
        gooner.moveLeft();
        
        assertEquals(initialVelocityX - 2, gooner.velocityX, 
                     "moveLeft() devrait réduire la vélocité X de 2");
    }

    @Test
    public void testMoveRight() {
        double initialVelocityX = gooner.velocityX;
        gooner.moveRight();
        
        assertEquals(initialVelocityX + 2, gooner.velocityX, 
                     "moveRight() devrait augmenter la vélocité X de 2");
    }

    @Test
    public void testStopX() {
        gooner.moveRight();
        gooner.moveRight();
        gooner.stopX();
        
        assertEquals(0, gooner.velocityX, "stopX() devrait réinitialiser la vélocité X");
    }

    @Test
    public void testYPositionUpdate() {
        gooner.velocityY = 5;
        double expectedY = gooner.y + 5;
        gooner.update();
        
        assertEquals(expectedY, gooner.y, 0.001, "La position Y devrait être mise à jour selon la vélocité");
    }

    @Test
    public void testXPositionUpdate() {
        gooner.velocityX = 3;
        double expectedX = gooner.x + 3;
        gooner.update();
        
        assertEquals(expectedX, gooner.x, 0.001, "La position X devrait être mise à jour selon la vélocité");
    }

    @Test
    public void testScreenWrapRight() {
        gooner.x = 380;
        gooner.velocityX = 50;
        gooner.update();
        
        assertTrue(gooner.x < 400, "Le Gooner devrait être ramené de l'autre côté à droite");
    }

    @Test
    public void testScreenWrapLeft() {
        gooner.x = -50;
        gooner.velocityX = 0;
        gooner.update();
        
        assertTrue(gooner.x >= 0, "Le Gooner devrait être ramené de l'autre côté à gauche");
    }

    @Test
    public void testCoinCount() {
        assertEquals(0, gooner.coins, "Le compte de pièces initial devrait être 0");
        gooner.coins += 5;
        assertEquals(5, gooner.coins, "Le compte de pièces devrait augmenter");
    }

    @Test
    public void testMultipleUpdates() {
        // Vérifier que les mises à jour multiples appliquent correctement la gravité
        for (int i = 0; i < 5; i++) {
            gooner.update();
        }
        
        assertTrue(gooner.velocityY > 0, "Après 5 mises à jour, la vélocité Y devrait être positive");
        assertTrue(gooner.y > 300, "Après 5 mises à jour, le Gooner devrait être tombé");
    }
}
