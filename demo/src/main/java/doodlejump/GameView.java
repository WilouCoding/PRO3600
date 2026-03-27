    package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;

import java.util.*;

public class GameView extends Pane {
    private boolean isGameOver = false;
    private Canvas canvas = new Canvas(400, 600);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private double standY = 280;
    private double standX = 180;
    private List<Platform> platforms = new ArrayList<>();
    private List<Monster> monsters = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    Gooner goon = new Gooner(standX, standY);

    private double cameraY = 0;
    private Random rand = new Random();
    private GamePanel scorePanel;
    private App app;

    public GameView(App app) {
        this.app = app;
        getChildren().add(canvas);
        scorePanel = new GamePanel((int) standY);
        getChildren().add(scorePanel); // Ajoute la fenêtre de score par-dessus le jeu
        generatePlatform(platforms);

        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now) {
                if (isGameOver) {
                    draw(goon, platforms);
                    return;
                }

                goon.update();
                scorePanel.updateScore(goon.y); // <-- Mise à jour du score ici !

                // --- Collision Gooner → Plateforme ---
                if (goon.velocityY > 0) {
                    for (Platform p : platforms) {
                        if (goon.x < p.x + p.WIDTH
                                && goon.x + Gooner.w > p.x
                                && goon.y + Gooner.h >= p.y
                                && goon.y + Gooner.h <= p.y + p.HEIGHT) {
                            goon.jump();
                        }
                    }
                }

                // --- Mise à jour Monstres ---
                for (Monster m : monsters) {
                    m.update();
                }

                // --- Mise à jour Balles ---
                for (Bullet b : bullets) {
                    b.update();
                }

                // --- Collision Balle → Monstre ---
                for (Bullet b : bullets) {
                    if (!b.active) continue;
                    for (Monster m : monsters) {
                        if (!m.isDead
                                && b.x < m.x + Monster.WIDTH
                                && b.x + Bullet.WIDTH > m.x
                                && b.y < m.y + Monster.HEIGHT
                                && b.y + Bullet.HEIGHT > m.y) {
                            m.isDead = true;
                            b.active = false;
                        }
                    }
                }

                // --- Collision Gooner → Monstre ---
                for (Monster m : monsters) {
                    if (m.isDead) continue;
                    if (goon.x < m.x + Monster.WIDTH
                            && goon.x + Gooner.w > m.x
                            && goon.y < m.y + Monster.HEIGHT
                            && goon.y + Gooner.h > m.y) {
                        // Sauter sur la tête du monstre → le tue et fait rebondir
                        if (goon.velocityY > 0 && goon.y + Gooner.h <= m.y + Monster.HEIGHT / 2.0) {
                            m.isDead = true;
                            goon.jump();
                        } else {
                            // Touché par le côté ou en dessous → Game Over
                            isGameOver = true;
                            scorePanel.setGameOver(true); // <-- On prévient le panel de score !
                        }
                    }
                } // <-- Il manquait toutes ces accolades de fermeture !

                // --- Game Over si chute hors écran ---
                if (goon.y > cameraY + 600) {
                    isGameOver = true;
                    scorePanel.setGameOver(true);
                }

                // --- Déplacement caméra ---
                if (goon.y < cameraY + 350) {
                    cameraY = goon.y - 350;
                }

                // --- Nettoyage des objets hors écran ---
                platforms.removeIf(p -> p.y - cameraY > 600);
                monsters.removeIf(m -> m.isDead || m.y - cameraY > 650);
                bullets.removeIf(b -> !b.active || b.y < cameraY - 50);

                // --- Génération de nouvelles plateformes ---
                while (platforms.size() < 11) {
                    double x = rand.nextDouble() * (400 - Platform.WIDTH);
                    double y = cameraY - rand.nextDouble() * Gooner.h;
                    platforms.add(new Platform(x, y));
                }

                // --- Spawn de monstres (max 3 à la fois, spawn aléatoire) ---
                if (monsters.size() < 3 && rand.nextInt(100) < 2) {
                    double x = rand.nextDouble() * (400 - Monster.WIDTH);
                    double y = cameraY - rand.nextDouble() * 400 - 100;
                    monsters.add(new Monster(x, y));
                }

                draw(goon, platforms);
            }
        };
        timer.start();
    }

    public void handleKeyPress(KeyCode code) {
        // Réinitialisation si Game Over
        if (isGameOver) {
            if (code == KeyCode.SPACE) {
                resetGame();
            }
            if (code == KeyCode.M) {
                app.showMenu(); // M = menu
            }
            return;
        }

        if (code == KeyCode.LEFT) {
            goon.moveLeft();
        } else if (code == KeyCode.RIGHT) {
            goon.moveRight();
        } else if (code == KeyCode.SPACE) {
            goon.jump();
        } else if (code == KeyCode.Z) {
            // Touche Z (AZERTY) pour tirer
            shoot();
        }
    }

    public void handleKeyRelease(KeyCode code) {
        if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            goon.stopX();
        }
    }

    private void shoot() {
        // Tire une balle depuis le centre haut du Gooner
        double bx = goon.x + Gooner.w / 2.0 - Bullet.WIDTH / 2.0;
        double by = goon.y;
        bullets.add(new Bullet(bx, by));
    }

    private void resetGame() {
        goon.x = standX;
        goon.y = standY;
        goon.velocityY = 0;
        cameraY = 0;
        platforms.clear();
        monsters.clear();
        bullets.clear();
        generatePlatform(platforms);
        isGameOver = false;
        scorePanel.reset();
    }

    public void draw(Gooner goon, List<Platform> platforms) {
        // Fond
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 400, 600);

        // Gooner
        gc.setFill(Color.BLUEVIOLET);
        gc.fillRect(goon.x, goon.y - cameraY, goon.w, goon.h);

        // Plateformes
        gc.setFill(Color.GRAY);
        for (Platform p : platforms) {
            gc.fillRect(p.x, p.y - cameraY, p.WIDTH, p.HEIGHT);
        }

        // Monstres
        gc.setFill(Color.RED);
        for (Monster m : monsters) {
            if (!m.isDead) {
                gc.fillRect(m.x, m.y - cameraY, Monster.WIDTH, Monster.HEIGHT);
            }
        }

        // Balles
        gc.setFill(Color.WHITE);
        for (Bullet b : bullets) {
            if (b.active) {
                gc.fillRect(b.x, b.y - cameraY, Bullet.WIDTH, Bullet.HEIGHT);
            }
        }        
    }

    public void generatePlatform(List<Platform> platforms) {
        Random random = new Random();
        for (int i = 0; i < 11; i++) {
            double x = random.nextDouble() * (400 - Platform.WIDTH);
            double y = 500 - i * Gooner.h;
            platforms.add(new Platform(x, y));
        }
    }
}
