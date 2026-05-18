package doodlejump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HighScoreManagerTest {
    private HighScoreManager highScoreManager;

    @BeforeEach
    public void setUp() {
        highScoreManager = new HighScoreManager();
    }

    @Test
    public void testInitialBestScore() {
        assertEquals(0, highScoreManager.getBestScore(), 
                     "Le meilleur score initial devrait être 0");
    }

    @Test
    public void testInitialTop5IsEmpty() {
        List<Integer> top5 = highScoreManager.getTop5();
        assertTrue(top5.isEmpty(), "La liste des top 5 devrait être vide au départ");
    }

    @Test
    public void testAddSingleScore() {
        highScoreManager.addScore(100);
        assertEquals(100, highScoreManager.getBestScore(), 
                     "Le meilleur score devrait être 100");
    }

    @Test
    public void testAddMultipleScores() {
        highScoreManager.addScore(50);
        highScoreManager.addScore(200);
        highScoreManager.addScore(150);
        
        assertEquals(200, highScoreManager.getBestScore(), 
                     "Le meilleur score devrait être le plus élevé (200)");
    }

    @Test
    public void testTop5Sorting() {
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        highScoreManager.addScore(150);
        highScoreManager.addScore(50);
        highScoreManager.addScore(175);
        
        List<Integer> top5 = highScoreManager.getTop5();
        
        assertEquals(5, top5.size(), "Devrait avoir 5 scores");
        assertEquals(200, top5.get(0), "Le premier devrait être 200");
        assertEquals(175, top5.get(1), "Le deuxième devrait être 175");
        assertEquals(150, top5.get(2), "Le troisième devrait être 150");
        assertEquals(100, top5.get(3), "Le quatrième devrait être 100");
        assertEquals(50, top5.get(4), "Le cinquième devrait être 50");
    }

    @Test
    public void testTop5LimitEnforced() {
        // Ajouter 10 scores
        for (int i = 100; i <= 1000; i += 100) {
            highScoreManager.addScore(i);
        }
        
        List<Integer> top5 = highScoreManager.getTop5();
        assertEquals(5, top5.size(), "Seulement 5 scores devraient être conservés");
    }

    @Test
    public void testTop5ContainsHighestScores() {
        // Ajouter des scores dans le désordre
        highScoreManager.addScore(10);
        highScoreManager.addScore(500);
        highScoreManager.addScore(20);
        highScoreManager.addScore(400);
        highScoreManager.addScore(30);
        highScoreManager.addScore(300);
        highScoreManager.addScore(40);
        highScoreManager.addScore(200);
        highScoreManager.addScore(50);
        highScoreManager.addScore(100);
        
        List<Integer> top5 = highScoreManager.getTop5();
        
        assertTrue(top5.contains(500), "Le top 5 devrait contenir 500");
        assertTrue(top5.contains(400), "Le top 5 devrait contenir 400");
        assertTrue(top5.contains(300), "Le top 5 devrait contenir 300");
        assertTrue(top5.contains(200), "Le top 5 devrait contenir 200");
        assertTrue(top5.contains(100), "Le top 5 devrait contenir 100");
        assertFalse(top5.contains(50), "Le top 5 ne devrait pas contenir 50");
    }

    @Test
    public void testAddDuplicateScores() {
        highScoreManager.addScore(100);
        highScoreManager.addScore(100);
        highScoreManager.addScore(100);
        
        List<Integer> top5 = highScoreManager.getTop5();
        assertEquals(3, top5.size(), "Les scores dupliqués devraient tous être ajoutés");
        assertEquals(100, top5.get(0));
        assertEquals(100, top5.get(1));
        assertEquals(100, top5.get(2));
    }

    @Test
    public void testAddZeroScore() {
        highScoreManager.addScore(0);
        assertEquals(0, highScoreManager.getBestScore());
        assertEquals(1, highScoreManager.getTop5().size());
    }

    @Test
    public void testLargeScores() {
        highScoreManager.addScore(999999);
        highScoreManager.addScore(888888);
        highScoreManager.addScore(777777);
        
        assertEquals(999999, highScoreManager.getBestScore(), 
                     "Devrait supporter de grands scores");
    }

    @Test
    public void testScoreOrderingIsDescending() {
        highScoreManager.addScore(30);
        highScoreManager.addScore(10);
        highScoreManager.addScore(50);
        highScoreManager.addScore(20);
        highScoreManager.addScore(40);
        
        List<Integer> top5 = highScoreManager.getTop5();
        
        for (int i = 0; i < top5.size() - 1; i++) {
            assertTrue(top5.get(i) >= top5.get(i + 1), 
                       "Les scores doivent être en ordre décroissant");
        }
    }

    @Test
    public void testAddScoreThenCheckBestScore() {
        highScoreManager.addScore(150);
        int bestBefore = highScoreManager.getBestScore();
        
        highScoreManager.addScore(200);
        int bestAfter = highScoreManager.getBestScore();
        
        assertEquals(150, bestBefore);
        assertEquals(200, bestAfter, "Le meilleur score devrait se mettre à jour");
    }
}
