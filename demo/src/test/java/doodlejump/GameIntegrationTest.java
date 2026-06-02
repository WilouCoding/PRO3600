package doodlejump;

import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GameIntegrationTest {

    @BeforeAll
    public static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            javafx.application.Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        latch.await();
    }

    @Test
    public void testHatBonusActivatesFlyingMode() throws Exception {
        GameView view = createGameView();

        Platform platform = new Platform(100, 300, false, false, false);
        Bonus bonus = new Bonus(platform, BonusType.HAT);

        setPrivateField(view, "bonuses", new ArrayList<>(List.of(bonus)));
        setPrivateField(view, "platforms", new ArrayList<>(List.of(platform)));

        view.goon.x = bonus.x;
        view.goon.y = bonus.y;
        view.goon.velocityY = 1.0;

        invokeTimerHandle(view, 1_000_000_000L);
        invokeTimerHandle(view, 1_000_016_666L);

        assertTrue((Boolean) getPrivateField(view, "isFlying"), "Le mode vol doit être activé après ramassage du chapeau");
        double flyTimer = (Double) getPrivateField(view, "flyTimer");
        assertTrue(flyTimer <= 1.5 && flyTimer > 1.4, "Le minuteur de vol doit être initialisé autour de HAT_FLIGHT_DURATION après la frame");
        double hatCooldown = (Double) getPrivateField(view, "hatCooldownTimer");
        assertTrue(hatCooldown <= 2.0 && hatCooldown > 1.9, "Le cool-down HAT doit être activé autour de 2.0 secondes");
        assertTrue(bonus.collected, "Le bonus doit être marqué comme collecté");
    }

    @Test
    public void testTrampolineBonusActivatesBackflip() throws Exception {
        GameView view = createGameView();

        Platform platform = new Platform(100, 300, false, false, false);
        Bonus bonus = new Bonus(platform, BonusType.TRAMPOLINE);

        setPrivateField(view, "bonuses", new ArrayList<>(List.of(bonus)));
        setPrivateField(view, "platforms", new ArrayList<>(List.of(platform)));

        view.goon.x = bonus.x;
        view.goon.y = bonus.y;
        view.goon.velocityY = 5.0;

        invokeTimerHandle(view, 1_000_000_000L);
        invokeTimerHandle(view, 1_000_016_666L);

        assertTrue(view.goon.velocityY < 0.0, "Le trampoline doit propulser le personnage vers le haut");
        assertTrue(view.goon.isBackflipping, "Le personnage doit entrer en backflip après le trampoline");
        assertTrue(bonus.collected, "Le bonus trampoline doit être marqué comme collecté");
    }

    @Test
    public void testFlyingModeEndsAfterDuration() throws Exception {
        GameView view = createGameView();

        setPrivateField(view, "isFlying", true);
        setPrivateField(view, "flyTimer", 0.02);
        setPrivateField(view, "isGameOver", false);
        setPrivateField(view, "awaitingSecondChanceChoice", false);

        invokeTimerHandle(view, 1_000_000_000L);
        invokeTimerHandle(view, 1_000_016_666L);

        assertFalse((Boolean) getPrivateField(view, "isFlying"), "Le mode vol doit se désactiver après épuisement du timer");
        assertTrue((Double) getPrivateField(view, "flyTimer") <= 0.0, "Le minuteur de vol doit être inférieur ou égal à zéro lorsqu'il est terminé");
    }

    private static GameView createGameView() throws InterruptedException {
        final GameView[] holder = new GameView[1];
        runAndWait(() -> {
            holder[0] = new GameView(new TestApp(), new CoinManager(), new ShopManager(), "player", new AccountManager());
            try {
                Object timer = getPrivateField(holder[0], "timer");
                Method stop = timer.getClass().getMethod("stop");
                stop.invoke(timer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return holder[0];
    }

    private static void invokeTimerHandle(GameView view, long now) throws Exception {
        Object timer = getPrivateField(view, "timer");
        setPrivateField(timer, "lastTime", now - 16_666_667L);
        Method handle = timer.getClass().getMethod("handle", long.class);

        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                handle.invoke(timer, now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }

    private static Object getPrivateField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setPrivateField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void runAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        javafx.application.Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
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
