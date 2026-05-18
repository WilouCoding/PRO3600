   package doodlejump;

import javafx.scene.paint.*;
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
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

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
    Gooner goon;
    public CoinManager coinManager = new CoinManager();
    private ShopManager shopManager;
    private AccountManager accountManager;
    private final String currentPlayerUsername;
    private List<Bonus> bonuses = new ArrayList<>();
    private int nextCoinScoreTarget = 200;
    private static final int COIN_SCORE_STEP = 200;
    private double flyTimer = 0.0; // Temps de vol restant (en secondes)
    private boolean isFlying = false;
    private double trampolineFlipAngle = 0.0;
    private double trampolineFlipRemaining = 0.0;
    private Image chapeauSkin = new Image(getClass().getResourceAsStream("/chapeau.png"));
    private double cameraY = 0;
    private Random rand = new Random(); 
    private GamePanel scorePanel;
    private App app;
    private VBox pauseMenu; // NOUVEAU : L'interface du menu pause

    public GameView(App app, CoinManager coinManager, ShopManager shopManager, String currentPlayerUsername, AccountManager accountManager) {
        this.app = app;
        this.coinManager = coinManager;
        this.shopManager = shopManager;
        this.currentPlayerUsername = currentPlayerUsername;
        this.accountManager = accountManager;
        ShopItem equippedSkin = shopManager.getEquippedSkin();
        String skinResource = equippedSkin != null && equippedSkin.skinResource != null
            ? equippedSkin.skinResource
            : "/gooner_skin.png";
        this.goon = new Gooner(standX, standY, skinResource);

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

                    if (trampolineFlipRemaining > 0) {
                        double deltaAngle = 720.0 * TIME_STEP;
                        trampolineFlipAngle += deltaAngle;
                        trampolineFlipRemaining -= deltaAngle;
                        if (trampolineFlipRemaining <= 0) {
                            trampolineFlipRemaining = 0;
                            trampolineFlipAngle = 0;
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
                                    savePlayerHighScore();
                                    scorePanel.setGameOver(true);
                                }
                            }
                        }
                        if (goon.y > cameraY + 600) {
                            isGameOver = true;
                            saveCollectedCoins();
                            savePlayerHighScore();
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
                                coinManager.addCoins(1);
                            }
                        }

                        coins.removeIf(c -> c.collected || c.y - cameraY > 600);

                        // Mise à jour et collision des bonus
                        for (Bonus b : bonuses) {
                            b.update(); // Pour qu'il suive la plateforme si elle bouge

                            // Collision simple (AABB) entre le Gooner et le Bonus
                            if (!b.collected && goon.x < b.x + Bonus.WIDTH && goon.x + Gooner.w > b.x 
                                && goon.y < b.y + Bonus.HEIGHT && goon.y + Gooner.h > b.y) {
                                if (b.type == BonusType.HAT) {
                                    b.collected = true;
                                    isFlying = true;
                                    flyTimer = 3.0; // 3 secondes de vol !
                                } else if (b.type == BonusType.TRAMPOLINE && goon.velocityY > 0) {
                                    b.collected = true;
                                    goon.velocityY = -15.0;
                                    trampolineFlipRemaining = 360.0;
                                    trampolineFlipAngle = 0.0;
                                }
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

                            // Si la plateforme n'est ni fragile, ni fantôme, on peut recevoir un bonus
                            if (!fragile && !ghost) {
                                int bonusChance = rand.nextInt(100);
                                if (bonusChance < 4) {
                                    bonuses.add(new Bonus(newP, BonusType.HAT));
                                } else if (bonusChance < 8) {
                                    bonuses.add(new Bonus(newP, BonusType.TRAMPOLINE));
                                }
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

    private void savePlayerHighScore() {
        if (currentPlayerUsername == null || currentPlayerUsername.isBlank()) {
            return;
        }
        if (accountManager != null) {
            accountManager.updatePlayerScore(currentPlayerUsername, scorePanel.getScore());
        }
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

    public void draw(Gooner goon, List<Platform> platforms) {
        drawSpaceBackground();
    
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
        for (Coin c : coins) {
            drawCoin(gc, c.x, c.y - cameraY, Coin.SIZE);
        }
        gc.setFont(Font.font("Arial", 16));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("🪙 " + goon.coins + "  (Total: " + coinManager.getCoins() + ")", 390, 30);
        gc.setTextAlign(TextAlignment.LEFT);
    }
//Création d'une méthode dédiée pour dessiner une pièce, ICI L'ARBRE DU FOND, pour éviter de surcharger la méthode draw
    private void drawTreeBackground() {
        double width = 400;
        double height = 600;

        LinearGradient sky = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#0c1710")),
            new Stop(0.45, Color.web("#12261b")),
            new Stop(1.0, Color.web("#0a140d"))
        );
        gc.setFill(sky);
        gc.fillRect(0, 0, width, height);

        double trunkX = 130;
        double trunkWidth = 140;
        double segment = 160;
        double offset = cameraY % segment;
        if (offset > 0) offset -= segment;

        for (double y = offset - segment; y < height + segment; y += segment) {
            gc.setFill(Color.web("#3f2a14"));
            gc.fillRoundRect(trunkX, y, trunkWidth, segment + 40, 44, 44);

            gc.setFill(Color.web("#523b1d"));
            gc.fillOval(trunkX + 12, y + 18, 40, 12);
            gc.fillOval(trunkX + 78, y + 58, 38, 14);
            gc.fillOval(trunkX + 22, y + 105, 38, 12);

            gc.setStroke(Color.web("#533915"));
            gc.setLineWidth(2.2);
            gc.strokeLine(trunkX + 36, y + 10, trunkX + 42, y + 70);
            gc.strokeLine(trunkX + 88, y + 30, trunkX + 94, y + 85);
            gc.strokeLine(trunkX + 60, y + 95, trunkX + 68, y + 140);
        }

        gc.setFill(Color.web("#1d2f1f", 0.9));
        for (double y = offset; y < height + segment; y += 120) {
            double branchY = y + 60;
            gc.fillOval(trunkX - 140, branchY, 120, 48);
            gc.fillOval(trunkX + trunkWidth + 20, branchY + 10, 120, 48);
            gc.fillOval(trunkX - 110, branchY + 14, 90, 34);
            gc.fillOval(trunkX + trunkWidth + 35, branchY + 24, 90, 34);
        }

        gc.setFill(Color.web("#0b1a0f", 0.7));
        for (double x = 0; x < width; x += 45) {
            gc.fillOval(x - 20, 20, 70, 40);
            gc.fillOval(x + 10, 140, 80, 50);
            gc.fillOval(x - 15, 320, 60, 30);
            gc.fillOval(x + 20, 480, 70, 45);
        }
    }

    private void drawSpaceBackground() {
        double width = 400;
        double height = 600;

        Stop[] stops = new Stop[] {
            new Stop(0.0, Color.web("#eff6ff")),
            new Stop(0.35, Color.web("#d0e4ff")),
            new Stop(0.75, Color.web("#a7c8ff")),
            new Stop(1.0, Color.web("#7ea8ff"))
        };
        LinearGradient sky = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        gc.setFill(sky);
        gc.fillRect(0, 0, width, height);

        double offset = cameraY % 120;
        if (offset > 0) offset -= 120;

        for (double y = offset; y < height + 120; y += 40) {
            for (double x = 15; x < width; x += 55) {
                double size = 1.5 + (Math.abs((x + y) % 20) * 0.09);
                gc.setFill(Color.web("#ffffff", 0.9));
                gc.fillOval(x, y + (x % 30) * 0.33, size, size);
                gc.setFill(Color.web("#dfe9ff", 0.55));
                gc.fillOval(x + 5, y + 7 + (x % 17) * 0.22, size * 0.7, size * 0.7);
            }
        }

        for (int i = 0; i < 5; i++) {
            double cx = 50 + i * 80;
            double cy = 130 + (i % 2) * 110 + offset * 0.8;
            RadialGradient nebula = new RadialGradient(
                0, 0,
                cx, cy,
                90,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#ffffff", 0.7)),
                new Stop(0.22, Color.web("#e8f1ff", 0.45)),
                new Stop(0.55, Color.web("#b9d3ff", 0.18)),
                new Stop(1.0, Color.web("#b9d3ff", 0.0))
            );
            gc.setFill(nebula);
            gc.fillOval(cx - 80, cy - 55, 160, 110);
        }

        for (int i = 0; i < 4; i++) {
            double cx = 90 + i * 95;
            double cy = 80 + (i % 3) * 130 + offset * 0.6;
            gc.setFill(Color.web("#f6f8ff", 0.35));
            gc.fillOval(cx, cy, 110, 45);
        }
    }

    private void drawGoonerWithOrientation(double x, double y) {
        gc.save();
        double centerX = x + Gooner.w / 2.0;
        double centerY = y + Gooner.h / 2.0;
        gc.translate(centerX, centerY);
        if (trampolineFlipRemaining > 0) {
            gc.rotate(trampolineFlipAngle);
        }

        if (goon.facingLeft) {
            gc.drawImage(goon.skin, -Gooner.w / 2.0, -Gooner.h / 2.0, -Gooner.w, Gooner.h);
        } else {
            gc.drawImage(goon.skin, -Gooner.w / 2.0, -Gooner.h / 2.0, Gooner.w, Gooner.h);
        }
        gc.restore();

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

    private void drawCoin(GraphicsContext gc, double x, double y, double size) {
        double centerX = x + size / 2;
        double centerY = y + size / 2;
        double radius = size / 2;

        // Ombre portée
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(x - 2, y + size * 0.6, size + 4, size * 0.3);

        // Base dorée foncée (ombre du dessous)
        gc.setFill(Color.web("#8B6914"));
        gc.fillOval(x + 1, y + 1, size - 2, size - 2);

        // Couche doée intermédiaire
        gc.setFill(Color.web("#DAA520"));
        gc.fillOval(x, y, size, size);

        // Dégradé d'or vers le jaune clair (lumière principale)
        gc.setFill(Color.web("#FFD700"));
        gc.fillOval(x + 2, y + 2, size - 4, size - 4);

        // Lumière réfléchie intense en haut-gauche
        gc.setFill(Color.web("#FFED4E"));
        gc.fillOval(x + size * 0.15, y + size * 0.15, size * 0.4, size * 0.35);

        // Petite zone super brillante (reflet)
        gc.setFill(Color.web("#FFFACD"));
        gc.fillOval(x + size * 0.25, y + size * 0.2, size * 0.2, size * 0.2);

        // Bordure en relief (double contour rétro)
        gc.setStroke(Color.web("#B8860B"));
        gc.setLineWidth(2);
        gc.strokeOval(x + 1, y + 1, size - 2, size - 2);

        gc.setStroke(Color.web("#DAA520"));
        gc.setLineWidth(1);
        gc.strokeOval(x, y, size, size);

        // Stries circulaires pour l'effet "pièce de monnaie"
        gc.setStroke(Color.web("#C4A200"));
        gc.setLineWidth(0.5);
        for (int i = 1; i <= 3; i++) {
            double r = radius * (0.3 + i * 0.15);
            gc.strokeOval(centerX - r, centerY - r, r * 2, r * 2);
        }

        // "G" en or foncé au centre (style gravure)
        gc.setFill(Color.web("#000000"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.5));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("G", centerX + 0.5, centerY + size * 0.15 + 0.5);

        // "G" en or clair par-dessus (relief)
        gc.setFill(Color.web("#2c0505"));
        gc.fillText("G", centerX, centerY + size * 0.15);

        gc.setTextAlign(TextAlignment.LEFT);
    }


}