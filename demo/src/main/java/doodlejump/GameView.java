   package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;         // NOUVEAU
import javafx.scene.control.Label;       // NOUVEAU
import javafx.scene.control.Button;      // NOUVEAU
import javafx.geometry.Pos;              // NOUVEAU
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

import java.util.*;

public class GameView extends Pane {
    private boolean isGameOver = false;
    private boolean isPaused = false; // NOUVEAU : État de la pause
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
    private VBox pauseMenu; // NOUVEAU : L'interface du menu pause

    public GameView(App app) {
        this.app = app;
        getChildren().add(canvas);
        scorePanel = new GamePanel((int) standY);
        getChildren().add(scorePanel); 

        createPauseMenu(); // NOUVEAU : On fabrique le menu
        getChildren().add(pauseMenu); // NOUVEAU : On l'ajoute par-dessus (il est invisible par défaut)

        generatePlatform(platforms);

        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = 0;
            private double accumulator = 0.0;
            private final double TIME_STEP = 1.0 / 60.0; 

            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // NOUVEAU : Si on est en pause, on met à jour lastTime pour éviter que l'accumulateur n'explose, et on arrête la logique
                if (isPaused) {
                    lastTime = now;
                    return; 
                }

                double elapsedTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                accumulator += elapsedTime;

                if (accumulator > 0.1) {
                    accumulator = 0.1;
                }

                while (accumulator >= TIME_STEP) {
                    if (!isGameOver) {
                        goon.update();
                        scorePanel.updateScore(goon.y);

                        //On prépare les 3 positions X possibles du Gooner
                        double[] xPositions = { goon.x, goon.x - 400, goon.x + 400 };

                        //Collision Gooner → Plateforme
                        if (goon.velocityY > 0) {
                            for (Platform p : platforms) {
                                // On vérifie si l'une des 3 positions touche la plateforme
                                for (double gx : xPositions) {
                                    if (gx < p.x + p.WIDTH && gx + Gooner.w > p.x && goon.y + Gooner.h >= p.y && goon.y + Gooner.h <= p.y + p.HEIGHT) {
                                        goon.jump();
                                        break; // On a touché, pas besoin de tester les autres fantômes
                                    }
                                }
                            }
                        }

                        //Mise à jour Monstres et Balles
                        for (Monster m : monsters) m.update();
                        for (Bullet b : bullets) b.update();

                        //Collision Balle → Monstre (Inchangée)
                        for (Bullet b : bullets) {
                            if (!b.active) continue;
                            for (Monster m : monsters) {
                                if (!m.isDead && b.x < m.x + Monster.WIDTH && b.x + Bullet.WIDTH > m.x && b.y < m.y + Monster.HEIGHT && b.y + Bullet.HEIGHT > m.y) {
                                    m.isDead = true;
                                    b.active = false;
                                }
                            }
                        }

                        //Collision Gooner → Monstre
                        for (Monster m : monsters) {
                            if (m.isDead) continue;
                            
                            // On vérifie si un des "fantômes" ou le vrai perso touche le monstre
                            boolean hit = false;
                            for (double gx : xPositions) {
                                if (gx < m.x + Monster.WIDTH && gx + Gooner.w > m.x && goon.y < m.y + Monster.HEIGHT && goon.y + Gooner.h > m.y) {
                                    hit = true;
                                    break;
                                }
                            }
                            
                            if (hit) {
                                if (goon.velocityY > 0 && goon.y + Gooner.h <= m.y + Monster.HEIGHT / 2.0) {
                                    m.isDead = true;
                                    goon.jump();
                                } else {
                                    isGameOver = true;
                                    scorePanel.setGameOver(true);
                                }
                            }
                        }
                        if (goon.y > cameraY + 600) {
                            isGameOver = true;
                            scorePanel.setGameOver(true);
                        }

                        if (goon.y < cameraY + 350) {
                            cameraY = goon.y - 350;
                        }

                        platforms.removeIf(p -> p.y - cameraY > 600);
                        monsters.removeIf(m -> m.isDead || m.y - cameraY > 650);
                        bullets.removeIf(b -> !b.active || b.y < cameraY - 50);

                        //Génération des plateformes 
                        while (platforms.size() < 11) {
                            //On cherche la plateforme la plus haute actuelle (le Y le plus petit)
                            double highestY = cameraY;
                            for (Platform p : platforms) {
                                if (p.y < highestY) {
                                    highestY = p.y;
                                }
                            }
                            
                            double x = rand.nextDouble() * (400 - Platform.WIDTH);
                            //La nouvelle plateforme se place toujours au-dessus de la plus haute (espacement garanti)
                            double y = highestY - (60 + rand.nextDouble() * 60);
                            platforms.add(new Platform(x, y));
                        }

                        //Spawn des monstres 
                        if (monsters.size() < 3 && rand.nextInt(100) < 2) {
                            double x = rand.nextDouble() * (400 - Monster.WIDTH);
                            double y = cameraY - 100 - rand.nextDouble() * 300; // Entre 100 et 400 pixels au-dessus de la caméra
                            
                            boolean isOverlapping = false;
                            
                            // On vérifie que le monstre ne spawn pas dans ou trop près d'une plateforme
                            for (Platform p : platforms) {
                                if (Math.abs(p.y - y) < 40 && Math.abs(p.x - x) < 80) {
                                    isOverlapping = true;
                                    break;
                                }
                            }
                            
                            // On vérifie qu'il ne spawn pas sur un autre monstre
                            for (Monster m : monsters) {
                                if (Math.abs(m.y - y) < 50 && Math.abs(m.x - x) < 50) {
                                    isOverlapping = true;
                                    break;
                                }
                            }
                            
                            // Si l'emplacement est libre, on spawn le monstre
                            if (!isOverlapping) {
                                monsters.add(new Monster(x, y));
                            }
                        }
                    }
                    accumulator -= TIME_STEP;
                }

                draw(goon, platforms);
            }
        };
        timer.start();
    }

    
    private void createPauseMenu() {
        pauseMenu = new VBox(15);
        pauseMenu.setAlignment(Pos.CENTER);
        // Fond noir semi-transparent
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-padding: 20; -fx-background-radius: 15;");
        pauseMenu.setPrefSize(300, 420);
        pauseMenu.setLayoutX(50); // Centré (400-300)/2
        pauseMenu.setLayoutY(90);

        Label title = new Label("PAUSE");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", 30));

        Label scoreLabel = new Label("Score actuel: 0");
        scoreLabel.setTextFill(Color.YELLOW);
        scoreLabel.setFont(Font.font("Arial", 20));
        
        VBox recordsBox = new VBox(5);
        recordsBox.setAlignment(Pos.CENTER);
        
        Button resumeBtn = new Button("Reprendre (P)");
        resumeBtn.setOnAction(e -> togglePause());

        Button restartBtn = new Button("Recommencer");
        restartBtn.setOnAction(e -> {
            togglePause();
            resetGame();
        });

        Button quitBtn = new Button("Quitter vers le Menu");
        quitBtn.setOnAction(e -> app.showMenu());

        String btnStyle = "-fx-font-size: 16px; -fx-background-color: #7132cf; -fx-text-fill: white; -fx-cursor: hand;";
        resumeBtn.setStyle(btnStyle);
        restartBtn.setStyle(btnStyle);
        quitBtn.setStyle(btnStyle);

        pauseMenu.getChildren().addAll(
            title, 
            scoreLabel, 
            new Label("--- RECORDS ---") {{ setTextFill(Color.WHITE); setFont(Font.font("Arial", 16)); }}, 
            recordsBox, 
            resumeBtn, 
            restartBtn, 
            quitBtn
        );
        pauseMenu.setVisible(false);
    }

    
    private void togglePause() {
        if (isGameOver) return; // Pas de pause quand on est mort

        isPaused = !isPaused;

        if (isPaused) {
            // Mise à jour du score actuel
            Label scoreLbl = (Label) pauseMenu.getChildren().get(1);
            scoreLbl.setText("Score actuel: " + scorePanel.getScore());

            
            VBox recordsBox = (VBox) pauseMenu.getChildren().get(3);
            recordsBox.getChildren().clear();
            List<Integer> top5 = scorePanel.getHighScoreManager().getTop5();
            for (int i = 0; i < top5.size(); i++) {
                Label l = new Label((i + 1) + ". " + top5.get(i));
                l.setTextFill(Color.WHITE);
                l.setFont(Font.font("Arial", 14));
                recordsBox.getChildren().add(l);
            }

            pauseMenu.setVisible(true);
        } else {
            pauseMenu.setVisible(false);
            this.requestFocus();
        }
    }

    public void handleKeyPress(KeyCode code) {

        if (code == KeyCode.P) {
            togglePause();
            return;
        }

        
        if (isPaused) return;

        if (isGameOver) {
            if (code == KeyCode.SPACE) resetGame();
            if (code == KeyCode.M) app.showMenu();
            return;
        }

        if (code == KeyCode.LEFT) goon.moveLeft();
        else if (code == KeyCode.RIGHT) goon.moveRight();
        else if (code == KeyCode.SPACE) goon.jump();
        else if (code == KeyCode.Z) shoot();
    }

    public void handleKeyRelease(KeyCode code) {
        if (isPaused) return; 
        if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            goon.stopX();
        }
    }

    private void shoot() {
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
        isPaused = false; // Par sécurité
        scorePanel.reset();
    }

    public void draw(Gooner goon, List<Platform> platforms) {
        // Fond
        gc.setFill(Color.web("#0a0a1a")); 
        gc.fillRect(0, 0, 400, 600);

        gc.setStroke(Color.web("#1a1a3a"));
        gc.setLineWidth(1.0);
    
        double gridSize = 40.0;
        double offset = -(cameraY % gridSize); 

        for (double y = offset; y < 600; y += gridSize) gc.strokeLine(0, y, 400, y);
        for (double x = 0; x < 400; x += gridSize) gc.strokeLine(x, 0, x, 600);

        gc.setFill(Color.BLUEVIOLET);
        gc.fillRoundRect(goon.x, goon.y - cameraY, goon.w, goon.h, 15, 15);
        gc.setStroke(Color.WHITE);
        gc.strokeRoundRect(goon.x, goon.y - cameraY, goon.w, goon.h, 15, 15);
    
        // Si le perso dépasse à droite, on dessine une copie à gauche
        if (goon.x + Gooner.w > 400) {
            gc.fillRoundRect(goon.x - 400, goon.y - cameraY, Gooner.w, Gooner.h, 15, 15);
            gc.strokeRoundRect(goon.x - 400, goon.y - cameraY, Gooner.w, Gooner.h, 15, 15);
        } 
        // Si le perso dépasse à gauche, on dessine une copie à droite
        else if (goon.x < 0) {
            gc.fillRoundRect(goon.x + 400, goon.y - cameraY, Gooner.w, Gooner.h, 15, 15);
            gc.strokeRoundRect(goon.x + 400, goon.y - cameraY, Gooner.w, Gooner.h, 15, 15);
        }

        gc.setFill(Color.GRAY);
        for (Platform p : platforms) {
            gc.fillRoundRect(p.x, p.y - cameraY, p.WIDTH, p.HEIGHT, 10, 10);
        }

        gc.setFill(Color.RED);
        for (Monster m : monsters) {
            if (!m.isDead) gc.fillRect(m.x, m.y - cameraY, Monster.WIDTH, Monster.HEIGHT);
        }

        gc.setFill(Color.WHITE);
        for (Bullet b : bullets) {
            if (b.active) gc.fillRect(b.x, b.y - cameraY, Bullet.WIDTH, Bullet.HEIGHT);
        }        
    }

    public void generatePlatform(List<Platform> platforms) {
        platforms.clear();
        
        //La plateforme de sécurité exacte sous les pieds du joueur
        platforms.add(new Platform(standX, standY + Gooner.h + 100));

        //Générer les 10 autres plateformes en montant progressivement
        double highestY = standY + Gooner.h;
        Random random = new Random();

        for (int i = 1; i < 11; i++) {
            double x = random.nextDouble() * (400 - Platform.WIDTH);
            //On espace chaque plateforme de 60 à 120 pixels de la précédente
            highestY -= (60 + random.nextDouble() * 60); 
            platforms.add(new Platform(x, highestY));
        }
    }
}