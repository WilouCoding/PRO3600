package doodlejump;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BonusTest {

    @Test
    public void testHatBonusSizeAndType() {
        Platform platform = new Platform(100, 200, false, false, false);
        Bonus bonus = new Bonus(platform, BonusType.HAT);

        assertEquals(BonusType.HAT, bonus.type, "Le bonus doit être de type HAT");
        assertEquals(25, bonus.width, 0.001, "La largeur du bonus HAT doit être 25");
        assertEquals(25, bonus.height, 0.001, "La hauteur du bonus HAT doit être 25");
        assertFalse(bonus.collected, "Le bonus ne doit pas être collecté à la création");
    }

    @Test
    public void testTrampolineBonusSizeAndType() {
        Platform platform = new Platform(100, 200, false, false, false);
        Bonus bonus = new Bonus(platform, BonusType.TRAMPOLINE);

        assertEquals(BonusType.TRAMPOLINE, bonus.type, "Le bonus doit être de type TRAMPOLINE");
        assertEquals(40, bonus.width, 0.001, "La largeur du bonus TRAMPOLINE doit être 40");
        assertEquals(35, bonus.height, 0.001, "La hauteur du bonus TRAMPOLINE doit être 35");
        assertFalse(bonus.collected, "Le bonus ne doit pas être collecté à la création");
    }

    @Test
    public void testBonusFollowsPlatformWhenUpdated() {
        Platform platform = new Platform(50, 150, false, true, false);
        Bonus bonus = new Bonus(platform, BonusType.HAT);
        double initialX = bonus.x;
        double initialY = bonus.y;

        platform.velocityX = 2.0;
        platform.update();
        bonus.update();

        assertNotEquals(initialX, bonus.x, "Le bonus doit se déplacer en X lorsque la plateforme bouge");
        assertEquals(platform.y - bonus.height, bonus.y, 0.001, "Le bonus doit rester au-dessus de la plateforme en Y");
    }
}
