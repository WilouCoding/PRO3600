package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlatformTest {
    private Platform platform;

    @BeforeEach
    public void setUp() {
        platform = new Platform(100, 200, false, false, false);
    }

    @Test
    public void testInitialPosition() {
        assertEquals(100, platform.x, "Position X devrait être 100");
        assertEquals(200, platform.y, "Position Y devrait être 200");
    }

    @Test
    public void testPlatformDimensions() {
        assertEquals(70, Platform.WIDTH, "Largeur devrait être 70");
        assertEquals(12, Platform.HEIGHT, "Hauteur devrait être 12");
    }

    @Test
    public void testFragilePlatform() {
        Platform fragile = new Platform(100, 200, true, false, false);
        assertTrue(fragile.isFragile, "La plateforme devrait être marquée comme fragile");
    }

    @Test
    public void testMovingPlatform() {
        Platform moving = new Platform(100, 200, false, true, false);
        assertTrue(moving.isMoving, "La plateforme devrait être marquée comme mobile");
    }

    @Test
    public void testGhostPlatform() {
        Platform ghost = new Platform(100, 200, false, false, true);
        assertTrue(ghost.isGhost, "La plateforme devrait être marquée comme fantôme");
    }

    @Test
    public void testStaticPlatformDoesNotMove() {
        double initialX = platform.x;
        platform.update();
        
        assertEquals(initialX, platform.x, "Une plateforme statique ne devrait pas bouger");
    }

    @Test
    public void testMovingPlatformMovesRight() {
        Platform movingRight = new Platform(100, 200, false, true, false);
        movingRight.velocityX = 2.0;
        double initialX = movingRight.x;
        movingRight.update();
        
        assertEquals(initialX + 2.0, movingRight.x, "La plateforme devrait se déplacer de 2 pixels à droite");
    }

    @Test
    public void testMovingPlatformMovesLeft() {
        Platform movingLeft = new Platform(100, 200, false, true, false);
        movingLeft.velocityX = -2.0;
        double initialX = movingLeft.x;
        movingLeft.update();
        
        assertEquals(initialX - 2.0, movingLeft.x, "La plateforme devrait se déplacer de 2 pixels à gauche");
    }

    @Test
    public void testPlatformBounceAtRightEdge() {
        Platform moving = new Platform(350, 200, false, true, false);
        moving.velocityX = 10.0; // Vitesse positive (vers la droite)
        moving.update();
        
        assertTrue(moving.velocityX < 0, "La vélocité devrait être inversée en touchant le bord droit");
    }

    @Test
    public void testPlatformBounceAtLeftEdge() {
        Platform moving = new Platform(5, 200, false, true, false);
        moving.velocityX = -10.0; // Vitesse négative (vers la gauche)
        moving.update();
        
        assertTrue(moving.velocityX > 0, "La vélocité devrait être inversée en touchant le bord gauche");
    }

    @Test
    public void testBounceCount() {
        assertEquals(0, platform.bounceCount, "bounceCount devrait être 0 au départ");
        platform.bounceCount++;
        assertEquals(1, platform.bounceCount, "bounceCount devrait être incrémenté");
    }

    @Test
    public void testMultiplePlatformTypes() {
        Platform p1 = new Platform(100, 200, false, false, false);
        Platform p2 = new Platform(150, 250, true, false, false);
        Platform p3 = new Platform(200, 300, false, true, false);
        Platform p4 = new Platform(250, 350, false, false, true);
        
        assertFalse(p1.isFragile || p1.isMoving || p1.isGhost);
        assertTrue(p2.isFragile && !p2.isMoving && !p2.isGhost);
        assertTrue(!p3.isFragile && p3.isMoving && !p3.isGhost);
        assertTrue(!p4.isFragile && !p4.isMoving && p4.isGhost);
    }

    @Test
    public void testMovingPlatformMultipleUpdates() {
        Platform moving = new Platform(100, 200, false, true, false);
        moving.velocityX = 1.0;
        
        for (int i = 0; i < 50; i++) {
            moving.update();
        }
        
        assertTrue(moving.x >= 0 && moving.x + Platform.WIDTH <= 400, 
                   "La plateforme devrait rester dans les limites après plusieurs updates");
    }
}
