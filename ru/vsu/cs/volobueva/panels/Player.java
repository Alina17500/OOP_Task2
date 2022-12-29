package ru.vsu.cs.volobueva.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;

import lombok.Getter;
import lombok.Setter;
import ru.vsu.cs.volobueva.engine.DominoBone;
import ru.vsu.cs.volobueva.engine.DominoGame;
import ru.vsu.cs.volobueva.engine.GameParameters;

public final class Player extends Game {
    private static final long serialVersionUID = -7224818727640107326L;

    @Getter
    @Setter
    public class Move {
        private DominoBone left;
        private DominoBone right;
    }

    @Getter
    @Setter
    private boolean isHuman;
    @Getter
    @Setter
    private boolean isGoButtonPressed;
    private JButton go = new JButton();
    private int xPlayer;
    private int yPlayer;
    private DominoGame dominoGame;

    public Player(String playerName, boolean human, DominoGame dominoGame) {
        this.setName(playerName);
        this.isHuman = human;
        this.dominoGame = dominoGame;
        setTitle(" Поле игрока " + getName() + " ");
    }

    MouseAdapter mouseAdapterGo = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent evt) {
            isGoButtonPressed = true;
            if (dominoGame.isFirstStep()) {
                dominoGame.firstMove();
            } else {
                dominoGame.nextMove();
            }
            evt.consume();
        }
    };

    public int endScore() { // сколько суммарно глаз осталось
        return getDominoBones().stream().mapToInt(dominoBone -> dominoBone.getSum()).sum();
    }

    public void addGoButton() { // показать кнопку хода
        go.setText("Сделать ход!");
        go.setLocation(GameParameters.MOVEJBX, GameParameters.MOVEJBY);
        go.addMouseListener(mouseAdapterGo);
        hideGoButton(); // изначально кнопка скрыта
        add(go, new AbsoluteConstraints(GameParameters.MOVEJBX, GameParameters.MOVEJBY, -1, -1));
    }

    public void showGoButton() { // показать кнопку хода
        go.setVisible(true);
    }

    public void hideGoButton() { // убрать кнопку хода
        go.setVisible(false);
    }

    void enableGoButton(String s) {
        go.setText(s);
        go.setEnabled(true);
        go.addMouseListener(mouseAdapterGo);
    }

    public void disableGoButton(String s) {
        go.setText(s);
        go.setEnabled(false);
        go.removeMouseListener(mouseAdapterGo);
    }

    void enableBonesSelect(DominoBone leftDominoBone, DominoBone rightDominoBone) { // разрешаем нажимать только подходящие домино
        xPlayer = GameParameters.XSHIFT;
        yPlayer = GameParameters.YSHIFT + GameParameters.SHIFT;

        boolean goodForLeft;
        boolean goodForRight;
        // если передан первый камень
        boolean isFirst = ((leftDominoBone != null) && (rightDominoBone != null))
                && ((leftDominoBone.isFirst()) && (rightDominoBone.isFirst()));

        for (DominoBone dominoBone : getDominoBones()) {
            if (leftDominoBone != null) {
                goodForLeft = isFirst ? dominoBone.isBoneGoodToMove(leftDominoBone.getLeft())
                        : dominoBone.isBoneGoodToMove(leftDominoBone.getWorkSide());
            } else {
                goodForLeft = false;
            }
            if (rightDominoBone != null) {
                goodForRight = isFirst ? dominoBone.isBoneGoodToMove(rightDominoBone.getRight())
                        : dominoBone.isBoneGoodToMove(rightDominoBone.getWorkSide());
            } else {
                goodForRight = false;
            }
            if (goodForLeft || goodForRight) { // разрешаем нажимать только те камни, что подходят по ситуации
                dominoBone.showFrame();
                dominoBone.addMouseListener(dominoBone.clickOnHumanPlayer);
            }
            dominoBone.setLocation(xPlayer, yPlayer);
            add(dominoBone, new AbsoluteConstraints(xPlayer, yPlayer, dominoBone.getWidth(), dominoBone.getHeight()));
            xPlayer += dominoBone.getWidth() + GameParameters.PLAYERSHIFT;
        }
    }

    public void selectPlayerBones(DominoBone dominoBone, DominoBone leftDominoBone, DominoBone rightDominoBone) { // Выбираем камень у игрока
        setSelectedLeft(null);
        setSelectedRight(null);

        getDominoBones().forEach(b -> {
            if ((!b.equals(dominoBone)) && (b.isSelected())) {
                b.toggleBoneSelection();
            } else if (b.equals(dominoBone)) {
                b.toggleBoneSelection();

                if (b.isSelected() && (leftDominoBone != null) && b.isBoneGoodToMove(leftDominoBone.getWorkSide())) {
                    setSelectedLeft(b);
                } else if (b.isSelected() && (rightDominoBone != null) && b.isBoneGoodToMove(rightDominoBone.getWorkSide())) {
                    setSelectedRight(b);
                }
            }
        });
        enableGoButton("Сделать ход!");
    }

    public String properScoreString(int i) {
        String s = "балл";
        if (((i > 1) && (i < 5)) || ((i > 20) && (((i % 10) > 1) && ((i % 10) < 5)))) {
            s += "ов";
        }
        return Integer.toString(i) + " " + s;
    }

    public String properBoneQtyString(int i) {
        String s = "домино";

        return Integer.toString(i) + " " + s;
    }

    public DominoBone firstBoneToStart() { // с какого камня заходит первый игрок (минимальный дупль либо камень)
        return hasDupletsAboveZero() ? minDupletAboveZero() : minBone();
    }

    boolean hasDuplets() { // есть ли дупли
        return getDominoBones().stream().anyMatch(dominoBone -> dominoBone.isDoublet());
    }

    boolean hasProperDuplet(byte boneSide) { // есть ли годные дупли
        return getDominoBones().stream().anyMatch(dominoBone -> dominoBone.isDoubletGoodToMove(boneSide));
    }

    boolean has2ProperDuplets(DominoBone leftDominoBone, DominoBone rightDominoBone) {
        return getDominoBones().stream().filter(dominoBone -> (dominoBone.isDoubletGoodToMove(leftDominoBone.getWorkSide()))
                || (dominoBone.isDoubletGoodToMove(rightDominoBone.getWorkSide()))).count() == 2;
    }

    public boolean hasDupletsAboveZero() { // есть ли дупли помимо 0:0
        return getDominoBones().stream().anyMatch(dominoBone -> (dominoBone.isDoublet() & dominoBone.getSum() > 0));
    }

    DominoBone minDuplet() {
        return getDominoBones().stream().filter(dominoBone -> dominoBone.isDoublet())
                .min((DominoBone b1, DominoBone b2) -> (b1.getSum() - b2.getSum())).orElse(null);
    }

    public DominoBone minDupletAboveZero() {
        return getDominoBones().stream().filter(dominoBone -> dominoBone.isDoublet() & dominoBone.getSum() > 0)
                .min((DominoBone b1, DominoBone b2) -> (b1.getSum() - b2.getSum())).orElse(null);
    }

    public DominoBone minBone() {
        return getDominoBones().stream().filter(dominoBone -> !dominoBone.isDoublet())
                .min((DominoBone b1, DominoBone b2) -> (b1.getSum() - b2.getSum())).orElse(null);
    }

    DominoBone properDuplet(byte boneSide) { // годный дупль
        return getDominoBones().stream().filter(dominoBone -> dominoBone.isDoubletGoodToMove(boneSide)).findFirst().orElse(null);
    }

    DominoBone maxProperBone(byte boneSide) { // максимально годный не-дупль для хода
        return getDominoBones().stream().filter(dominoBone -> dominoBone.isBoneGoodToMove(boneSide))
                .max((DominoBone b1, DominoBone b2) -> (b1.getSum() - b2.getSum())).orElse(null);
    }

    public Move putBones(Panel panel) { // возвращаем массив двух камней, левый и правый
        DominoBone fieldLeft = panel.leftBone();
        DominoBone fieldRight = panel.rightBone();
        Move move = new Move();
        byte left, right; // левые и правые части на поле для хода

        if ((fieldLeft.isFirst()) && (fieldRight.isFirst())) { // если идем от первого камня
            left = fieldLeft.getLeft();
            right = fieldRight.getRight();
        } else if ((fieldLeft.isFirst()) && (!fieldRight.isFirst())) { // если левый камень самый первый
            left = fieldLeft.getLeft();
            right = fieldRight.getWorkSide();
        } else if ((!fieldLeft.isFirst()) && (fieldRight.isFirst())) { // если правый камень самый первый
            left = fieldLeft.getWorkSide();
            right = fieldRight.getRight();
        } else { // если минимум три камня, левый, первый, и правый
            left = fieldLeft.getWorkSide();
            right = fieldRight.getWorkSide();
        }
        move.left = maxProperBone(left);
        move.right = maxProperBone(right);

        if ((move.left != null) && (move.right != null)) { // если подходят камни с двух сторон, выбираем больший по
                                                           // сумме глаз
            if (move.left.getSum() > move.right.getSum()) {
                move.right = null;
            } else if (move.left.getSum() <= move.right.getSum()) {
                move.left = null;
            }
        }
        if (hasProperDuplet(left) && (left != right)) { // если есть подходящий дупль слева, берем его
            move.left = properDuplet(left);
            move.right = null;
        }
        if (hasProperDuplet(right)) { // если есть подходящий дупль справа, берем его
            move.left = null;
            move.right = properDuplet(right);
        }
        if ((left != right) && (hasProperDuplet(left)) && (hasProperDuplet(right))) { // если два подходящих дупля,
                                                                                      // отдупляемся :))
            move.left = properDuplet(left);
            move.right = properDuplet(right);
        }
        return move;
    }

    public boolean less7Bones() { // есть ли 7 камней на борту
        return getDominoBones().size() < GameParameters.MAXBONES;
    }

    public boolean has7Bones() {
        return getDominoBones().size() == GameParameters.MAXBONES;
    }

    public String playerMsg() { // Сообщение на панель поля
        String s = " Ходит " + getName() + ". ";
        return (isHuman) ? s + "Выбирайте нужное домино на панели и домино для вашего хода. Потом делайте ход. "
                : s + "Нажмите кнопку на панели ";
    }

    @Override
    protected void rebuildBonesLine(boolean frame) { // выстраиваем камни в рядок
        xPlayer = GameParameters.XSHIFT;
        yPlayer = GameParameters.YSHIFT + GameParameters.SHIFT;

        for (DominoBone dominoBone : getDominoBones()) {
            dominoBone.removeMouseListener(dominoBone.clickOnHumanPlayer);

            if (dominoBone.isSelected()) {
                dominoBone.unselect();
            }
            dominoBone.hideFrame();
            if (isHuman) {
                dominoBone.showBone();
            } else {
                dominoBone.hideBone();
            }
            dominoBone.setLocation(xPlayer, yPlayer);
            add(dominoBone, new AbsoluteConstraints(xPlayer, yPlayer, dominoBone.getWidth(), dominoBone.getHeight()));
            xPlayer += dominoBone.getWidth() + GameParameters.PLAYERSHIFT;
        }
        repaint();
    }

    public void disableBonesSelect() {
        rebuildBonesLine(GameParameters.NOFRAME);
    }

    @Override
    public void addToBones(DominoBone dominoBone) {
        dominoBone.removeMouseListener(dominoBone.clickOnShop); // отменяем базарные нажатия мышкой
        dominoBone.draw(GameParameters.Corner.C90.getCorner(), GameParameters.UNSELECTED);
        getDominoBones().add(dominoBone);
        disableBonesSelect();
        setTitle(" " + getName() + " имеет " + properBoneQtyString(getDominoBones().size()) + " "); // обновляем заголовок панели
    }

    @Override
    public void removeFromBones(DominoBone dominoBone) { // вызываем папин метод и обновляем заголовок панели
        super.removeFromBones(dominoBone);
        disableBonesSelect();
        setTitle(" " + getName() + " имеет " + properBoneQtyString(getDominoBones().size()) + " "); // обновляем заголовок панели
    }

    @Override
    public void setTitle(String title) {
        this.setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 20), new Color(12, 11, 11)));
    }
}
