   package doodlejump;

import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;         // NOUVEAU
import javafx.scene.control.Label;       // NOUVEAU
import javafx.scene.image.Image;
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
    private List<Coin> coins = new ArrayList<>();
    private Set<String> input = new HashSet<>();
    Gooner goon = new Gooner(standX, standY);
    public CoinManager coinManager = new CoinManager();
    private List<Bonus> bonuses = new ArrayList<>();
    private int nextCoinScoreTarget = 200;
    private static final int COIN_SCORE_STEP = 200;
    private double flyTimer = 0.0; // Temps de vol restant (en secondes)
    private boolean isFlying = false;
    private Image chapeauSkin = new Image(getClass().getResourceAsStream("/chapeau.png"));

    private double cameraY = 0;
    private Random rand = new Random(); 
    private GamePanel scorePanel;
    private App app;
    private VBox pauseMenu; // NOUVEAU : L'interface du menu pause

    public GameView(App app) {
        this.app = app;
        this.setOnKeyPressed(e -> {
            input.add(e.getCode().toString());
        });

        this.setOnKeyReleased(e -> {
            input.remove(e.getCode().toString());
        });

        this.setFocusTraversable(true); // Important pour capter le clavier
        this.setOnKeyPressed(e -> input.add(e.getCode().toString()));
        this.setOnKeyReleased(e -> input.remove(e.getCode().toString()));

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
                    if (isFlying) {
                        flyTimer -= TIME_STEP; // TIME_STEP vaut 1/60e de seconde
                        goon.velocityY = -8;   // Le personnage monte tout seul (vitesse constante)
                        
                        if (flyTimer <= 0) {
                            isFlying = false;  // Fin du bonus après 5 secondes
                        }
                    }

                    if (!isGameOver) {
                        goon.update();
                        scorePanel.updateScore(goon.y);

                        //On prépare les 3 positions X possibles du Gooner
                        double[] xPositions = { goon.x, goon.x - 400, goon.x + 400 };

                        for (Platform p : platforms) {
                            p.update();

                            //Collision Gooner → Plateforme
                            if (goon.velocityY > 0) { // Le perso descend
                                boolean collisionDetectee = false;
            
                                // On vérifie la collision pour le perso réel ET ses fantômes
                                for (double gx : xPositions) {
                                    if (gx < p.x + Platform.WIDTH 
                                        && gx + Gooner.w > p.x 
                                        && goon.y + Gooner.h >= p.y 
                                        && goon.y + Gooner.h <= p.y + Platform.HEIGHT) {
                    
                                        collisionDetectee = true;
                                        break; // Une collision trouvée suffit
                                        }
                                    }

                                if (collisionDetectee) {
                                    if (p.isGhost) {
                                        // On ne fait pas sauter le Gooner.
                                        // On marque juste la plateforme pour qu'elle disparaisse.
                                        p.bounceCount++; 
                                    } else {
                                        // C'est une plateforme normale, fragile ou mobile
                                        goon.jump(); 
                                        if (p.isFragile) {
                                            p.bounceCount++;
                                        }
                                    }
                                        
                                }
                                
                            }
                        }

                        // Nettoyage des plateformes cassées (à mettre juste après la boucle for)
                        platforms.removeIf(p -> p.isFragile && p.bounceCount >= 1);

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
                                    saveCollectedCoins();
                                    scorePanel.setGameOver(true);
                                }
                            }
                        }
                        if (goon.y > cameraY + 600) {
                            isGameOver = true;
                            saveCollectedCoins();
                            scorePanel.setGameOver(true);
                        }

                        // Collecte des pièces
                        for (Coin c : coins) {
                            if (c.collected) continue;
                            boolean collected = false;
                            for (double gx : xPositions) {
                                if (gx < c.x + Coin.SIZE && gx + Gooner.w > c.x && goon.y < c.y + Coin.SIZE && goon.y + Gooner.h > c.y) {
                                    collected = true;
                                    break;
                                }
                            }
                            if (collected) {
                                c.collected = true;
                                goon.coins++;
                            }
                        }

                        coins.removeIf(c -> c.collected || c.y - cameraY > 600);

                        // Mise à jour et collision des bonus
                        for (Bonus b : bonuses) {
                            b.update(); // Pour qu'il suive la plateforme si elle bouge

                            // Collision simple (AABB) entre le Gooner et le Bonus
                            if (!b.collected && goon.x < b.x + Bonus.WIDTH && goon.x + Gooner.w > b.x 
                                && goon.y < b.y + Bonus.HEIGHT && goon.y + Gooner.h > b.y) {
                                
                                b.collected = true;
                                isFlying = true;
                                flyTimer = 3.0; // 3 secondes de vol !
                            }
                        }

                        // Nettoyage des bonus ramassés ou hors écran
                        bonuses.removeIf(b -> b.collected || b.y - cameraY > 600);

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

                            // Si on est monté assez haut (cameraY est négatif et diminue), 
                            // on a par exemple 25% de chance d'avoir une plateforme fragile et 30% de mobile
                            boolean fragile = false;
                            boolean moving = false;
                            boolean ghost = false;

                            if(cameraY < -6000) {
                                int chance = rand.nextInt(100);
                                if (chance < 30) fragile = true;      // 30% de chance d'être fragile
                                else if (chance < 70) moving = true; // 40% de chance d'être mobile
                                else if (chance < 85) ghost = true;   // 15% de chance d'être fantôme
                            
                            // Si on est à hauteur moyenne de -6000, des fragiles + des mobiles, et un peu de fantômes
                            } else if (cameraY < -6000) {
                                int chance = rand.nextInt(100);
                                if (chance < 20) fragile = true;      // 20% de chance d'être fragile
                                else if (chance < 30) moving = true; // 30% de chance d'être mobile
                                else if (chance < 60) ghost = true;   // 10% de chance d'être fantôme
                                
                            // Si on est à hauteur moyenne de -4000, des fragiles + mobiles  
                            } else if (cameraY < -4000) {
                                int chance = rand.nextInt(100);
                                if (chance < 20) fragile = true;      // 20% de chance d'être fragile
                                else if (chance < 40) moving = true; // 20% de chance d'être mobile
                            } 
                            // Si on est à hauteur moyenne de -2000, seulement des fragiles
                            else if (cameraY < -2000) {
                                if (rand.nextInt(100) < 20) fragile = true;
                            }

                            Platform newP = new Platform(x, y, fragile, moving, ghost);
                            platforms.add(newP);

                            if (scorePanel.getScore() >= nextCoinScoreTarget) {
                                coins.add(new Coin(x + Platform.WIDTH / 2 - Coin.SIZE / 2, y - Coin.SIZE - 10));
                                nextCoinScoreTarget += COIN_SCORE_STEP;
                            }

                            //Si la plateforme n'est ni fragile, ni fantôme, on a 5% de chance d'y mettre un jetpack
                            if (!fragile && !ghost && rand.nextInt(100) < 2) {
                                bonuses.add(new Bonus(newP));
                            }
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
        coins.clear();
        generatePlatform(platforms);
        isGameOver = false;
        isPaused = false; // Par sécurité
        isFlying = false;
        flyTimer = 0.0;
        bonuses.clear();
        scorePanel.reset();
        goon.coins = 0;
    }

    private void saveCollectedCoins() {
        if (goon.coins > 0) {
            coinManager.addCoins(goon.coins);
            goon.coins = 0;
        }
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
    
        // Si le perso dépasse à droite, on dessine une copie à gauche
        if (goon.x + Gooner.w > 400) {
            drawGoonerWithOrientation(goon.x - 400, goon.y - cameraY);
        } 
        // Si le perso dépasse à gauche, on dessine une copie à droite
        else if (goon.x < 0) {
            drawGoonerWithOrientation(goon.x + 400, goon.y - cameraY);
        }

        // Dessin du personnage principal (toujours appelé)
        drawGoonerWithOrientation(goon.x, goon.y - cameraY);

        for (Platform p : platforms) {
            if (p.isMoving) {
                gc.setFill(Color.LIGHTBLUE); // Plateforme mobile
            } else if (p.isFragile) {
                gc.setFill(Color.BROWN);
            } else if (p.isGhost) {
                gc.setGlobalAlpha(0.5); // Rend la plateforme à moitié transparente
                gc.setFill(Color.YELLOW);
            } else {
                gc.setFill(Color.GRAY);
            }

            // fillRoundRect(x, y, largeur, hauteur, rayon_largeur, rayon_hauteur)
            gc.fillRoundRect(p.x, p.y - cameraY, Platform.WIDTH, Platform.HEIGHT, 15, 15);
            gc.setGlobalAlpha(1.0);

            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRoundRect(p.x, p.y - cameraY, Platform.WIDTH, Platform.HEIGHT, 15, 15);
        
            if (p.isFragile) {
                gc.setStroke(Color.YELLOW); // Couleur de l'éclair
                gc.setLineWidth(2);         // Un peu plus épais pour qu'on le voie bien

                // On calcule le centre de la plateforme
                double midX = p.x + Platform.WIDTH / 2;
                double midY = p.y - cameraY;

                // On dessine un éclair en 3 segments (Z-shape)
                // Segment 1 : du haut vers le milieu
                gc.strokeLine(midX + 5, midY + 2, midX - 5, midY + 6);
                // Segment 2 : le retour au milieu
                gc.strokeLine(midX - 5, midY + 6, midX + 5, midY + 6);
                // Segment 3 : du milieu vers le bas
                gc.strokeLine(midX + 5, midY + 6, midX - 5, midY + 10);
            }   
        }
        
        if (goon.facingLeft) {
            // On dessine l'image inversée
            // x + w : on décale le point de départ à droite
            // -w : on dessine vers la gauche pour créer l'effet miroir
            gc.drawImage(goon.skin, goon.x + Gooner.w, goon.y - cameraY, -Gooner.w, Gooner.h);
        } else {
            // Dessin normal vers la droite
            gc.drawImage(goon.skin, goon.x, goon.y - cameraY, Gooner.w, Gooner.h);
        }

        for (Bonus b : bonuses) {
            if (!b.collected) {
                gc.drawImage(b.skin, b.x, b.y - cameraY, Bonus.WIDTH, Bonus.HEIGHT);
            }
        }

        if (isFlying) {
            gc.setFill(Color.ORANGE);
            gc.setFont(Font.font("Arial", 16));
            gc.fillText("Mode Vol : " + String.format("%.1f", flyTimer) + "s", 10, 80);
        }

        for (Monster m : monsters) {
            if (!m.isDead) {
                // On dessine le skin du monstre à la place du rectangle
                gc.drawImage(m.skin, m.x, m.y - cameraY, Monster.WIDTH, Monster.HEIGHT);
            }
        }

        gc.setFill(Color.WHITE); // Couleur blanche
        for (Bullet b : bullets) {
            if (b.active) {
                // On utilise fillOval au lieu de fillRect
                //WIDTH et HEIGHT doivent être identiques pour faire un rond
                gc.fillOval(b.x, b.y - cameraY, Bullet.WIDTH, Bullet.HEIGHT);
                
                // Optionnel : un petit effet de lueur (glow) pour les rendre plus visibles
                gc.setGlobalAlpha(0.3);
                gc.fillOval(b.x - 2, (b.y - cameraY) - 2, Bullet.WIDTH + 4, Bullet.HEIGHT + 4);
                gc.setGlobalAlpha(1.0);
            }
        }        
        gc.setFill(Color.YELLOW);
        for (Coin c : coins) {
            gc.fillOval(c.x, c.y - cameraY, Coin.SIZE, Coin.SIZE);
        }   
        gc.setFill(Color.YELLOW);
        gc.setFont(Font.font("Arial", 16));
        gc.fillText("🪙 " + goon.coins + "  (Total: " + coinManager.getCoins() + ")", 10, 80);
    }

    public void generatePlatform(List<Platform> platforms) {
        platforms.clear();
        
        //La plateforme de sécurité exacte sous les pieds du joueur
        platforms.add(new Platform(standX, standY + Gooner.h + 100, false, false, false));

        //Générer les 10 autres plateformes en montant progressivement
        double highestY = standY + Gooner.h;
        Random random = new Random();

        for (int i = 1; i < 11; i++) {
            double x = random.nextDouble() * (400 - Platform.WIDTH);
            //On espace chaque plateforme de 60 à 120 pixels de la précédente
            highestY -= (60 + random.nextDouble() * 60); 
            platforms.add(new Platform(x, highestY, false, false, false));
            if (random.nextInt(10000) < 30) {
                coins.add(new Coin(x + Platform.WIDTH / 2 - Coin.SIZE / 2, highestY - Coin.SIZE - 10));
            }
        }
    }

    private void drawGoonerWithOrientation(double x, double y) {
        if (goon.facingLeft) {
            // Dessin inversé (Miroir) : on décale de 'w' et on dessine sur '-w'
            gc.drawImage(goon.skin, x + Gooner.w, y, -Gooner.w, Gooner.h);
        } else {
            // Dessin normal
            gc.drawImage(goon.skin, x, y, Gooner.w, Gooner.h);
        }

        if (isFlying) {
            // On récupère une image de bonus disponible pour avoir le skin (ou on utilise une variable dédiée)
            // on décale le dessin pour le mettre sur sa tête.
            double bonusW = Bonus.WIDTH;
            double bonusH = Bonus.HEIGHT;
            
            // On centre le bonus horizontalement par rapport au Gooner, 
            // et on le place juste au-dessus de sa tête (y - bonusH)
            double bonusX = x + (Gooner.w / 2) - (bonusW / 2);
            double bonusY = y - bonusH + 8; // +8 pour qu'il soit légèrement enfoncé sur sa tête (comme un chapeau)

            if (goon.facingLeft) {
                // Chapeau en miroir
                gc.drawImage(chapeauSkin, bonusX + bonusW, bonusY, -bonusW, bonusH);
            } else {
                // Chapeau normal
                gc.drawImage(chapeauSkin, bonusX, bonusY, bonusW, bonusH);
            }
        }
    }
}