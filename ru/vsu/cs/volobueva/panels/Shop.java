package ru.vsu.cs.volobueva.panels;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;

import ru.vsu.cs.volobueva.engine.DominoBone;
import ru.vsu.cs.volobueva.engine.DominoGame;
import ru.vsu.cs.volobueva.engine.GameParameters;

public final class Shop extends Game {

    private static final long serialVersionUID = -4485166148555484926L;
    private int xShop;
    private int yShop;
    private DominoGame dominoGame;

    public Shop(DominoGame dominoGame) {
        this.dominoGame = dominoGame;
        xShop = GameParameters.XSHIFT;
        yShop = GameParameters.YSHIFT - 2 * GameParameters.SHIFT;
        setDominoBones(initBones());
        initShop();
        setTitle(" Магазин ");
    }

    private void initShop() {
        getDominoBones().forEach(dominoBone -> {
            addToBones(dominoBone);
            yShop += GameParameters.BONEY + GameParameters.SHIFT;
        });
    }

    private List<DominoBone> initBones() { // инициализируем домино
        List<DominoBone> dominoBones = new ArrayList<>(GameParameters.TOTALBONES);
        for (byte i = 0; i <= GameParameters.MAXDOTS; i++) {
            for (byte j = i; j <= GameParameters.MAXDOTS; j++) {
                dominoBones.add(new DominoBone(i, j, dominoGame));
            }
        }
        Collections.shuffle(dominoBones);
        return dominoBones;
    }

    public DominoBone randomFromBones() { // произвольный домино с базара
        return getDominoBones().isEmpty() ? null : getDominoBones().get((new Random()).nextInt(getDominoBones().size()));
    }

    @Override
    protected void addToBones(DominoBone dominoBone) {
        dominoBone.setLocation(xShop, yShop);
        dominoBone.addMouseListener(dominoBone.clickOnShop); // обработчик нажатий
        add(dominoBone, new AbsoluteConstraints(xShop, yShop, GameParameters.BONEX, GameParameters.BONEY));
        repaint();
    }

    @Override
    protected void setTitle(String title) {
        setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), new Color(12, 11, 11)));
    }

    @Override
    protected void rebuildBonesLine(boolean frame) {
        getDominoBones().forEach(dominoBone -> {
            if (frame == GameParameters.NOFRAME) {
                dominoBone.removeMouseListener(dominoBone.clickOnShop); // убираем обработку мыши и рамку для всех домино
                dominoBone.hideFrame();
            } else {
                dominoBone.addMouseListener(dominoBone.clickOnShop); // добавляем обработку мыши и рамку для всех домино
                dominoBone.showFrame();
            }
        });
        repaint();
    }

    public void enableShop() {
        rebuildBonesLine(GameParameters.FRAME);
    }

    public void disableShop() {
        rebuildBonesLine(GameParameters.NOFRAME);
    }
}
