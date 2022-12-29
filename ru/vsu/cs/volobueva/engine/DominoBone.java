package ru.vsu.cs.volobueva.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import lombok.Getter;
import lombok.Setter;

public final class DominoBone extends JButton {
    private static final long serialVersionUID = 1756065351166502914L;
    @Getter
    private byte left; // левая часть кости
    @Getter
    private byte right; // правая часть кости
    @Getter
    @Setter
    private byte workSide; // сторона, к которой ставим домино
    @Getter
    private final int sum;
    @Getter
    @Setter
    private boolean isFirst;
    @Getter
    @Setter
    private boolean isDoublet;
    @Getter
    private boolean isSelected;
    @Getter
    private int angle;
    private BufferedImage faceImage = null;
    private BufferedImage backImage = null;
    private ImageIcon face;
    private ImageIcon back;
    private final DominoGame dominoGame;
    private Random randomizer = new Random();

    public boolean equals(DominoBone dominoBone) {
        return ((this.left == dominoBone.left) && (this.right == dominoBone.right))
                || ((this.left == dominoBone.right) && (this.right == dominoBone.left));
    }

    public final MouseAdapter clickOnShop = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            dominoGame.setShopSelectedDominoBone((DominoBone) event.getSource()); // нажатая костяшка;
            if (dominoGame.isGet7bones()) {
                dominoGame.getStart7BonesFromShop();
            }
            if (dominoGame.isNeedMoreBones()) {
                dominoGame.getMoreBonesFromShop();
            }
            event.consume();
        }
    };

    public final MouseAdapter clickOnHumanPlayer = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            dominoGame.setPlayerSelectedDominoBone((DominoBone) event.getSource()); // нажатая костяшка;
            dominoGame.getCurrentPlayer().selectPlayerBones(dominoGame.getPlayerSelectedDominoBone(),
                    dominoGame.getPanel().getSelectedLeft(), dominoGame.getPanel().getSelectedRight());
            event.consume();
        }
    };

    public final MouseAdapter clickOnField = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            dominoGame.setSelectedOnPanelDominoBone((DominoBone) event.getSource()); // нажатая костяшка;
            dominoGame.getPanel().selectFieldBones(dominoGame.getCurrentPlayer(), dominoGame.getSelectedOnPanelDominoBone());
            event.consume();
        }
    };

    @Override
    public String toString() {
        return ((isDoublet == GameParameters.DUPLET) ? "doublet " : "dominoBone ") + left + ":" + right;
    }

    public boolean isDoubletGoodToMove(byte boneSide) { // подходит ли дупль для хода
        return (isDoublet == GameParameters.DUPLET) && (left == boneSide) && (right == boneSide);
    }

    public boolean isBoneGoodToMove(byte boneSide) { // можно ли ходить костью
        return ((left == boneSide) || (right == boneSide));
    }

    void invertBone() { // переворачиваем домино, меняем лево-право
        byte temp = left;
        left = right;
        right = temp;
        angle = Math.abs(180 - angle); // угол увеличиваем на 180 и берем модуль
    }

    private BufferedImage invertImg(BufferedImage img) { // инвертируем изображение для выбора домино
        BufferedImage invertImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB); // картинка
                                                                                                                   // как
                                                                                                                   // и
                                                                                                                   // исходная
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int rgba = img.getRGB(i, j);
                Color color = new Color(rgba, true);

                color = new Color(255 - color.getRed(), // инвертируем цвета
                        255 - color.getGreen(), 255 - color.getBlue());
                invertImg.setRGB(i, j, color.getRGB());
            }
        }
        return invertImg;
    }

    private BufferedImage createFaceImg(BufferedImage img1, BufferedImage img2) { // склеиваем две картинки в одну
        int imgWidth = 0;
        int imgHeight = 0;

        if ((angle == GameParameters.Corner.C0.getCorner()) || (angle == GameParameters.Corner.C180.getCorner())) { // домино горизонтально,
                                                                                            // размер
            imgWidth = (2 * img1.getWidth()) + GameParameters.OFFSET;
            imgHeight = img1.getHeight();
        } else if ((angle == GameParameters.Corner.C90.getCorner()) || (angle == GameParameters.Corner.C270.getCorner())) { // домино
                                                                                                    // вертикально,
                                                                                                    // размер
            imgWidth = img1.getWidth();
            imgHeight = (2 * img1.getHeight()) + GameParameters.OFFSET;
        }

        BufferedImage boneImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = boneImg.createGraphics();
        Color oldColor = graphics.getColor();
        graphics.setPaint(Color.WHITE);
        graphics.fillRect(0, 0, imgWidth, imgHeight);
        graphics.setColor(oldColor);

        if ((angle == GameParameters.Corner.C0.getCorner()) || (angle == GameParameters.Corner.C180.getCorner())) { // домино горизонтально
            graphics.drawImage(img1, null, 0, 0);
            graphics.drawImage(img2, null, GameParameters.OFFSET + img2.getWidth(), 0);
        } else if ((angle == GameParameters.Corner.C90.getCorner()) || (angle == GameParameters.Corner.C270.getCorner())) { // домино
                                                                                                    // вертикально
            graphics.drawImage(img1, null, 0, 0);
            graphics.drawImage(img2, null, 0, GameParameters.OFFSET + img2.getHeight());
        }
        graphics.dispose();
        return boneImg;
    }

    public void draw(int angle, boolean selected) { // отрисовываем домино
        String prefix = ""; // путь к картинкам домино
        int width = GameParameters.BONEX; // по умолчанию домино горизонтально
        int height = GameParameters.BONEY;
        this.angle = angle;

        if ((angle == GameParameters.Corner.C0.getCorner()) || (angle == GameParameters.Corner.C180.getCorner())) { // если горизонтально
            prefix = "/img/bones/horizontal/";
        } else if ((angle == GameParameters.Corner.C90.getCorner()) || (angle == GameParameters.Corner.C270.getCorner())) { // если
                                                                                                    // вертикально
            prefix = "/img/bones/vertical/";
            int temp = width;
            width = height;
            height = temp;
        }

        if ((angle == GameParameters.Corner.C270.getCorner()) || (angle == GameParameters.Corner.C180.getCorner())) { // при перевороте домино
                                                                                              // меняем местами
                                                                                              // лево-право
            invertBone();
        }
        setSize(new Dimension(width, height)); // ставим размеры домино
        try {
            URL img1Url = getClass().getResource(prefix + left + ".png");
            URL img2Url = getClass().getResource(prefix + right + ".png");
            URL backUrl = getClass().getResource(prefix + "back.png");
            BufferedImage img1 = ImageIO.read(Objects.requireNonNull(img1Url));
            BufferedImage img2 = ImageIO.read(Objects.requireNonNull(img2Url));

            faceImage = (selected == GameParameters.UNSELECTED) ? createFaceImg(img1, img2)
                    : invertImg(createFaceImg(img1, img2));
            backImage = ImageIO.read(Objects.requireNonNull(backUrl));
        } catch (IOException ex) {
        }
        face = new ImageIcon(faceImage);
        back = new ImageIcon(backImage);
    }

    void select() {
        draw(angle, GameParameters.SELECTED);
        isSelected = GameParameters.SELECTED;
        showBone();
    }

    public final void unselect() {
        draw(angle, GameParameters.UNSELECTED);
        isSelected = GameParameters.UNSELECTED;
        showBone();
    }

    public final void toggleBoneSelection() {
        if (isSelected) {
            unselect();
        } else {
            select();
        }
    }

    public final void showFrame() {
        setBorderPainted(true);
    }

    public final void hideFrame() {
        setBorderPainted(false);
    }

    public final void showBone() { // костями вверх
        setIcon(face);
    }

    public final void hideBone() { // костями вниз
        setIcon(back);
    }

    public DominoBone(byte left, byte right, DominoGame dominoGame) {
        this.dominoGame = dominoGame;
        if (randomizer.nextBoolean()) { // костяшки переворачиваются случайным образом
            this.left = left;
            this.right = right;
        } else {
            this.left = right;
            this.right = left;
        }
        sum = left + right;
        isDoublet = (left == right);

        draw(GameParameters.Corner.C0.getCorner(), GameParameters.UNSELECTED); // для базара домино лежат ровно
        setPreferredSize(new Dimension(GameParameters.BONEX, GameParameters.BONEY));
        showFrame(); // показываем рамку для набора на базаре
        hideBone(); // в начале
    }
}
