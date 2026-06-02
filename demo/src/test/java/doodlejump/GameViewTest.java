package doodlejump;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GameViewTest {
    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @Test
    public void testSpaceDoesNotJump() throws InterruptedException {
        GameView view = createGameView();
        double initialVelocityY = view.goon.velocityY;

        runAndWait(() -> view.handleKeyPress(KeyCode.SPACE));

        assertEquals(initialVelocityY, view.goon.velocityY, 0.0001,
                     "Espace ne doit pas faire sauter le personnage");
    }

    @Test
    public void testSDoesNotBackflip() throws InterruptedException {
        GameView view = createGameView();

        runAndWait(() -> view.handleKeyPress(KeyCode.S));

        assertFalse(view.goon.isBackflipping, "S ne doit pas déclencher de backflip");
    }

    private static GameView createGameView() throws InterruptedException {
        final GameView[] viewHolder = new GameView[1];
        runAndWait(() -> {
            viewHolder[0] = new GameView(new TestApp(), new CoinManager(), new ShopManager(), "player", new AccountManager());
        });
        return viewHolder[0];
    }

    private static void runAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            action.run();
            latch.countDown();
        });
        latch.await();
    }

    private static class TestApp extends App {
        @Override
        public void start(Stage stage) {}

        @Override
        public void showMenu() {}

        @Override
        public void showShop() {}

        @Override
        public void showAccountMenu() {}
    }
}
