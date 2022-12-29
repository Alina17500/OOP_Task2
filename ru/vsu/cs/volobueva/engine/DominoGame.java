package ru.vsu.cs.volobueva.engine;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import ru.vsu.cs.volobueva.panels.Shop;
import ru.vsu.cs.volobueva.panels.Player;
import ru.vsu.cs.volobueva.panels.Panel;

@Log
public final class DominoGame extends JFrame {
    //private static final long serialVersionUID = -4761309140419685336L;
    private Shop shop;
    @Getter
    private Panel panel;
    private Player me;
    private Player you;
    private final String myName = "Alina";
    private final String enemyName = chooseEnemy();
    @Getter
    private boolean isFirstStep = true;
    @Getter
    private boolean isGet7bones = true;
    @Getter
    private boolean isNeedMoreBones;
    private DominoBone left;
    private DominoBone right;
    @Setter
    private DominoBone shopSelectedDominoBone = null;
    @Getter
    @Setter
    private DominoBone playerSelectedDominoBone = null;
    @Getter
    @Setter
    private DominoBone selectedOnPanelDominoBone = null;
    @Getter
    private Player currentPlayer = null;

    public void play() {
        setVisible(true);
    }

    public DominoGame() {
        try {
            LogManager.getLogManager()
                    .readConfiguration(DominoGame.class.getResourceAsStream("/properties/logging.properties"));
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        setTitle("Домино: " + myName + " играет против " + enemyName);
        setIconImage((new ImageIcon(Objects.requireNonNull(getClass().getResource("/img/logos/domino.png")))).getImage());
        initComponents();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            }
        });
        Arrays.stream(UIManager.getInstalledLookAndFeels()).filter(lf -> lf.getName().equalsIgnoreCase("metal"))
                .forEach(lf -> {
                    try {
                        UIManager.setLookAndFeel(lf.getClassName());
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                            | UnsupportedLookAndFeelException e) {
                        log.severe("Error UI manager");
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    private void initComponents() {
        panel = new Panel(enemyName);
        shop = new Shop(this);
        you = new Player(enemyName, false, this);
        me = new Player(myName, GameParameters.HUMAN, this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(GameParameters.SIZEX, GameParameters.SIZEY));
        setResizable(false);
        setSize(new Dimension(GameParameters.SIZEX, GameParameters.SIZEY));
        getContentPane().setLayout(new AbsoluteLayout());

        getContentPane().add(shop, new AbsoluteConstraints(1200, 0, 100, 720));
        getContentPane().add(you, new AbsoluteConstraints(0, 0, 1200, 100));
        getContentPane().add(me, new AbsoluteConstraints(0, 620, 1200, 100));
        getContentPane().add(panel, new AbsoluteConstraints(0, 100, 1200, 520));

        pack();
        setLocationRelativeTo(null);
    }

    private Object yourEnemy() {
        return GameParameters.ENEMY.keySet().toArray()[new Random().nextInt(GameParameters.ENEMY.keySet().toArray().length)];
    }

    private String chooseEnemy() { // Показываем диалоговое окно на старте, пока не выберем соперника или не выйдем
        String enemy = "";
        int choice = JOptionPane.NO_OPTION;

        while (choice != JOptionPane.YES_OPTION) {
            enemy = (String) yourEnemy();
            choice = JOptionPane.showConfirmDialog(null, "Ваш противник: " + enemy, "Начать игру?",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    new ImageIcon(Objects.requireNonNull(DominoGame.class.getResource("/img/logos/" + GameParameters.ENEMY.get(enemy)))));

            if (choice == JOptionPane.CANCEL_OPTION) {
                System.exit(0);
            }
        }
        return enemy;
    }

    private Player nextPlayer() { // кто ходит следующим
        return (currentPlayer == me) ? you : me;
    }

    private Player whoFirst(Player... player) { // Выясняем, чей первый ход
        return Arrays.stream(player).filter(Player::hasDupletsAboveZero)
                .min(Comparator.comparingInt((Player p) -> p.minDupletAboveZero().getSum()))
                .orElse(Arrays.stream(player)
                        .min(Comparator.comparingInt((Player p) -> p.minBone().getSum())).get());
    }

    private String getFinalMessage(GameParameters.End endCase) {
        List<String> strings = new ArrayList<>();

        if (endCase == GameParameters.End.NORMAL) {
            strings.add("Выиграл " + currentPlayer.getName() + "!");
        } else if (endCase == GameParameters.End.FISH) {
            strings.add("У " + currentPlayer.getName() + " осталось "
                    + currentPlayer.properScoreString(currentPlayer.endScore()));
        }
        strings.add("У " + nextPlayer().getName() + " осталось "
                + nextPlayer().properScoreString(nextPlayer().endScore()));

        int max = strings.stream().max(Comparator.comparingInt(String::length)).get().length();
        return strings.stream()
                .map(s -> System.lineSeparator()
                        + IntStream.range(0, max - s.length()).mapToObj(i -> " ").collect(Collectors.joining("")) + s)
                .reduce((s1, s2) -> s1 + s2).get();
    }

    private void gameEnd(GameParameters.End endCase) { // конец
        panel.disableBonesSelect();
        panel.setTitle(" Игра окончена ");
        shop.showBones();
        shop.disableShop();

        currentPlayer.setHuman(GameParameters.HUMAN);
        currentPlayer.showBones();
        currentPlayer.disableBonesSelect();
        currentPlayer.hideGoButton();
        currentPlayer.setTitle(" " + currentPlayer.getName() + " : "
                + currentPlayer.properScoreString(currentPlayer.endScore()) + " ");

        nextPlayer().setHuman(GameParameters.HUMAN);
        nextPlayer().showBones();
        nextPlayer().disableBonesSelect();
        nextPlayer().hideGoButton();
        nextPlayer().setTitle(
                " " + nextPlayer().getName() + " : " + nextPlayer().properScoreString(nextPlayer().endScore()) + " ");

        JOptionPane.showMessageDialog(null, getFinalMessage(endCase), "Конец!", JOptionPane.INFORMATION_MESSAGE);
        //log.info("GameParameters ended!");
    }

    void getStart7BonesFromShop() {
        if ((me.less7Bones()) && (you.less7Bones())) { // набираем кости с базара
            // берем от клика мыши
            me.addToBones(shopSelectedDominoBone);
            shop.removeFromBones(shopSelectedDominoBone);

            shopSelectedDominoBone = shop.randomFromBones(); // берем случайную кость с базара
            you.addToBones(Objects.requireNonNull(shopSelectedDominoBone));
            shop.removeFromBones(shopSelectedDominoBone);

            panel.setTitle(" Возьмите еще " + me.properBoneQtyString(GameParameters.MAXBONES - me.getDominoBones().size()) + " ");
        }

        if ((me.has7Bones()) && (you.has7Bones())) { // набрали
            isGet7bones = false;
            shop.disableShop();
            prepareFirstMove(); // готов к первому ходу
        }
    }

    private void prepareFirstMove() { // Выясняем первого игрока и готовим первый ход
        currentPlayer = whoFirst(me, you); // кто ходит первым
        currentPlayer.addGoButton(); // добавляем кнопки хода
        nextPlayer().addGoButton();
        currentPlayer.showGoButton(); // у первого игрока кнопку показываем, у следующего она скрыта

        panel.setTitle(" Первый ходит " + currentPlayer.getName() + ", у кого наименьший номинал "
                + currentPlayer.firstBoneToStart() + ". Нажмите кнопку на панели ");
    }

    public void firstMove() {
        if (currentPlayer.isGoButtonPressed()) {
            panel.addFirstBone(currentPlayer.firstBoneToStart()); // ставим первый домино на поле
            currentPlayer.removeFromBones(currentPlayer.firstBoneToStart());

            currentPlayer.hideGoButton(); // убираем кнопку хода у первого игрока
            currentPlayer.setGoButtonPressed(false);
            currentPlayer = nextPlayer(); // передаем ход следующему
            currentPlayer.showGoButton(); // показываем кнопку у следующего

            panel.setTitle(currentPlayer.playerMsg()); // сообщение на поле
            isFirstStep = false; // больше первых ходов не будет

            if (currentPlayer.isHuman() == GameParameters.HUMAN) {
                panel.enableFieldSelect(currentPlayer);
                currentPlayer.disableGoButton("Выбирайте");
            } else {
                panel.disableBonesSelect();
            }

            left = currentPlayer.putBones(panel).getLeft(); // ход человека
            right = currentPlayer.putBones(panel).getRight();

            if ((currentPlayer.isHuman() == GameParameters.HUMAN) && (left == null) && (right == null)) { // если у человека нет
                                                                                                // домино, заставляем
                                                                                                // идти в магаз
                panel.setTitle(" " + currentPlayer.getName() + " нечем ходить, берите в магазине ");
                currentPlayer.disableGoButton("Взять в магазине");
                panel.disableBonesSelect();
                shop.enableShop();

                isNeedMoreBones = true;
            }
        }
    }

    void getMoreBonesFromShop() { // берем домино в магазине по ходу игры
        if (currentPlayer.isHuman() == GameParameters.ROBOT) { // если робот, берет сам и ходит
            while (!shop.getDominoBones().isEmpty()) {
                shopSelectedDominoBone = shop.randomFromBones();

                currentPlayer.addToBones(Objects.requireNonNull(shopSelectedDominoBone));
                shop.removeFromBones(shopSelectedDominoBone);

                left = currentPlayer.putBones(panel).getLeft();
                right = currentPlayer.putBones(panel).getRight();

                if ((left != null) || (right != null)) {
                    break;
                }
            }
        } else if (currentPlayer.isHuman() == GameParameters.HUMAN) {
            currentPlayer.addToBones(shopSelectedDominoBone);
            shop.removeFromBones(shopSelectedDominoBone);

            left = currentPlayer.putBones(panel).getLeft();
            right = currentPlayer.putBones(panel).getRight();

            if ((left != null) || (right != null)) {
                panel.setTitle(" " + currentPlayer.getName() + " можно делать ход ");
                panel.enableFieldSelect(currentPlayer);
                shop.disableShop(); // если взяли подходящий домино, запрещаем магаз
                currentPlayer.disableGoButton("Берите");
            }
        }
    }

    public void nextMove() {
        if (currentPlayer.isGoButtonPressed()) {
            if (isNeedMoreBones) { // если человек набирал домино с базара, так уже все.
                isNeedMoreBones = false;
                shop.disableShop();
            }

            panel.setTitle(nextPlayer().playerMsg()); // сообщение на поле

            if (currentPlayer.isHuman() == GameParameters.ROBOT) {
                left = currentPlayer.putBones(panel).getLeft(); // ход игрока
                right = currentPlayer.putBones(panel).getRight();
            } else if (currentPlayer.isHuman() == GameParameters.HUMAN) {
                left = currentPlayer.getSelectedLeft();
                right = currentPlayer.getSelectedRight();
            }

            if ((currentPlayer.isHuman() == GameParameters.ROBOT) && (left == null) && (right == null)) {
                if (!shop.getDominoBones().isEmpty()) {
                    getMoreBonesFromShop();
                } else {
                    if ((nextPlayer().putBones(panel).getLeft() == null)
                            && (nextPlayer().putBones(panel).getRight() == null)) {
                        gameEnd(GameParameters.End.FISH);
                    }
                }
            }

            if (left != null) {
                panel.addToLeft(left);
                currentPlayer.removeFromBones(left);
            }

            if (right != null) {
                panel.addToRight(right);
                currentPlayer.removeFromBones(right);
            }

            if (currentPlayer.getDominoBones().size() > 0) { // играем дальше, домино еще есть
                currentPlayer.hideGoButton(); // скрыли кнопку
                currentPlayer.setGoButtonPressed(false);
                currentPlayer = nextPlayer(); // передали ход
                currentPlayer.showGoButton(); // показали кнопку}

                left = currentPlayer.putBones(panel).getLeft(); // ход человека
                right = currentPlayer.putBones(panel).getRight();

                if ((currentPlayer.isHuman() == GameParameters.HUMAN)) { // человек
                    if ((left == null) && (right == null)) { // нечем ходить
                        if (!shop.getDominoBones().isEmpty()) {
                            panel.setTitle(
                                    " " + currentPlayer.getName() + " нечем ходить, берите в магазине ");
                            currentPlayer.disableGoButton("В магазин");
                            shop.enableShop(); // разрешаем брать из магаза
                            panel.disableBonesSelect();
                            isNeedMoreBones = true;
                        } else {
                            if ((nextPlayer().putBones(panel).getLeft() == null)
                                    && (nextPlayer().putBones(panel).getRight() == null)) {
                                gameEnd(GameParameters.End.FISH);
                            }
                        }

                    } else { // есть чем ходить
                        panel.enableFieldSelect(currentPlayer);
                        currentPlayer.disableGoButton("Берите");
                    }
                } else if (currentPlayer.isHuman() == GameParameters.ROBOT) { // при ходе робота клацать мышкой не даем
                    panel.disableBonesSelect();
                }

            } else { // выкинули все домино
                gameEnd(GameParameters.End.NORMAL);
            }
        }
    }
}
