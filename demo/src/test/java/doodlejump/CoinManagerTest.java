package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CoinManagerTest {
    private CoinManager coinManager;

    @BeforeEach
    public void setUp() {
        coinManager = new CoinManager();
    }

    @Test
    public void testInitialCoinCount() {
        assertEquals(0, coinManager.getCoins(), 
                     "Le nombre de pièces initial devrait être 0 si pas de fichier existant");
    }

    @Test
    public void testAddCoins() {
        coinManager.addCoins(10);
        assertEquals(10, coinManager.getCoins(), "Ajouter 10 pièces devrait donner 10");
    }

    @Test
    public void testAddMultipleCoins() {
        coinManager.addCoins(5);
        coinManager.addCoins(3);
        coinManager.addCoins(7);
        
        assertEquals(15, coinManager.getCoins(), 
                     "Ajouter 5 + 3 + 7 pièces devrait donner 15");
    }

    @Test
    public void testAddLargeAmountOfCoins() {
        coinManager.addCoins(1000);
        assertEquals(1000, coinManager.getCoins(), "Devrait supporter de grands nombres");
    }

    @Test
    public void testAddZeroCoins() {
        coinManager.addCoins(0);
        assertEquals(0, coinManager.getCoins(), "Ajouter 0 pièces ne devrait pas changer le compte");
    }

    @Test
    public void testAddNegativeCoins() {
        coinManager.addCoins(10);
        coinManager.addCoins(-5);
        assertEquals(5, coinManager.getCoins(), 
                     "Ajouter des pièces négatives devrait réduire le compte");
    }

    @Test
    public void testMultipleAdditionsAreAccumulative() {
        for (int i = 1; i <= 10; i++) {
            coinManager.addCoins(i);
        }
        
        // 1+2+3+4+5+6+7+8+9+10 = 55
        assertEquals(55, coinManager.getCoins(), 
                     "Les additions doivent s'accumuler correctement");
    }
}
